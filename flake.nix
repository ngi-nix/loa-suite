{
  inputs = {
    mvn2nix.url = "github:fzakaria/mvn2nix";
    nixpkgs.url = "nixpkgs/nixos-21.05";
  };

  outputs = { self, nixpkgs, mvn2nix, ... }:
    let
      supportedSystems =
        [ "x86_64-linux" "x86_64-darwin" "i686-linux" "aarch64-linux" ];
      forAllSystems = f:
        nixpkgs.lib.genAttrs supportedSystems (system: f system);
    in rec {

      defaultPackage = forAllSystems (system:
        (import nixpkgs {
          # ./overlay.nix contains the logic to package local repository
          overlays = [
            mvn2nix.overlay
            (final: prev: {

              loa-app = with final;
                let
                  mavenRepository = buildMavenRepositoryFromLockFile {
                    file = ./mvn2nix-lock.json;
                  };

                  # Because the stripped down version of JRE is not working JDK is used as runtime for now.
                  jre = jdk11_headless;
                  # jre = pkgs.jre_minimal.override {
                  #   modules = [
                  #     "java.base"
                  #     "java.logging"
                  #     "java.desktop"
                  #     "java.naming"
                  #     "java.management"
                  #   ];
                  #   jdk = pkgs.jdk11_headless;
                  # };

                in stdenv.mkDerivation rec {
                  pname = "loa-app";
                  version = "0.0.7";
                  name = "${pname}-${version}";
                  src = ./.;
                  patches = [ ./remove-maven-git-plugin.patch ];
                  nativeBuildInputs = [ jdk11_headless maven makeWrapper ];
                  buildPhase = ''
                    mvn package --offline -Dmaven.repo.local=${mavenRepository}
                  '';

                  installPhase = ''
                    mkdir -p $out/bin
                    ln -s ${mavenRepository} $out/lib
                    cp loa-app/loa-app-spring-boot/target/app.jar $out/

                    makeWrapper ${jre}/bin/java $out/bin/${pname} \
                          --add-flags "-jar $out/app.jar"
                  '';
                };
            })
          ];
          inherit system;
        }).loa-app);

      packages = forAllSystems (system: rec {
        loa-app = self.defaultPackage."${system}";
        docker = let pkgs = (import nixpkgs { inherit system; });
        in pkgs.dockerTools.buildLayeredImage {
          name = "loa-app";
          tag = "latest";
          created = "now";
          contents = loa-app;
          config = {
            Cmd = [ "/bin/loa-app" ];
            WorkingDir = "/data";
          };
        };
      });

      overlay = final: prev: { loa-app = defaultPackage; };

      nixosModules.loa-app = { config, pkgs, lib, ... }:
        let cfg = config.services.loa-app;

        in with lib; {
          options = {
            services.loa-app = { enable = mkEnableOption "loa-app"; };
          };

          config = {
            nixpkgs.overlays = self.overlays;

            systemd.services = mkIf cfg.enable {
              loa-app = {
                description = "loa-app";
                wantedBy = [ "multi-user.target" ];
                after = [ "network.target" ];
                serviceConfig = {
                  ExecStart = "${pkgs.loa-app}/bin/loa-app";
                  DynamicUser = true;
                  StateDirectory = "loa-app";
                  WorkingDirectory = "/var/lib/private/loa-app";
                };
              };
            };
          };
        };
    };
}

