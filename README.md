[[_TOC_]]

# loa-suite
This is the linked-open-actors main project. A so called Maven-Multi-Module project containing all loa projects.

# Project structure
![project_structure](doc/img/project_structure.svg "project_structure")

- **loa-suite**
    - **development-environment**  
    Everybody who want can create it's folder here an place scripts to start the app, container, docker-compose. (fh-> Fred Hauschel)
    - **loa-app**  
    the loa-app is a sample application holds some administrative stuff and is used for testing. The loa-app should not be used for business logic or loa-actor specific stuff. However it is the main thing to start and for now the only thing to start. This is because we are more or less in alpha/beta and all services are part of this loa-app. You can see it as a service container ;-)
        - **loa-app-controller**  
        MVC-Controller and REST-Controller layer, only for experimental stuff! Real controllers has to be located in their own modules.
        - **loa-app-spring-boot**  
        Runtime Environment! This is the thing that ends in a executable jar, that is part of the docker image
    - **loa-adapters**  
    Place for code that works as adapter, wrapper, bridge, etc. between the LOA World and the world of other data providers.
        - [loa-kvm-adapter](loa-adapters/loa-kvm-adapter/README.md)  
        Adapters for https://kartevonmorgen.org/ or more https://github.com/kartevonmorgen/openfairdb
        - [loa-wechange-adapter](loa-adapters/loa-wechange-adapter/README.md)  
        Adapter for the WECHANGE api. WECHANGE is already a LOA-Provider !
        - **loa-csv-adapter** 
        WorkInProgress - Importing LOA Data from csv. Initial imports, that also creates a new Storage for a LOA-Actor.
        - **loa-osm-adapter**  
        OpenStreetMap Adapter (Since our Hackathon 2021 - WorkInProgress)
    - **loa-algorithms**  
        - **loa-algorithm**  
        Common  stuff like interfaces, etc.
        - **loa-fuzzySearchAlgorithms**  
        Based on https://github.com/xdrop/fuzzywuzzy
        - **loa-distanceCalculator**  
        For distance calculaton between geo locations
    - **loa-repository**  
    Accessing (CRUD) RDF objects that are stored in a LOA RDF Store. (WorkInProgress!)
    - **loa-comparators** 
    A Comparator knows how to compare a set of properties with a specific algorithm.
        - **loa-comparator**  
        Common  stuff like interfaces, etc.
        - **loa-comparator-geo-coordinates**  
        Compares the geo coordinates (distance) of two loa-publications/loa-organisations 
    - **loa-similarity-checkers**  
        - **loa-similarity-checker**  
        Common  stuff like interfaces, etc.
        - **loa-similarity-checker-hackathon2021**  
        Our first similarity-checker that was started on our hackathon 2021.

# Starting loa-app with docker image
The loa-app is a spring-boot app and it can be started as spring-boot app. The most users maybe prefer to use the generated docker image, wich is available in the [gitlab docker registry](https://gitlab.com/linkedopenactors/loa-suite/container_registry/1865362).

There is a latest image of each git-branch. Normally you should use the latest image from master branch!

`docker run -p8080:8080 registry.gitlab.com/linkedopenactors/loa-suite:master`

After the image is started, you can access the app: http://localhost:8090/

**Available Web Service Endpoints:**  
http://localhost:8090/swagger-ui.html

**DevOp stuff:**  
http://localhost:8090/actuator


# Development Environment
Currently there is one instance running, that is public available.  
This is our [development instance](https://loa.test.opensourceecology.de/), that is provided by [Makers4Humanity](https://www.m4h.network/) & [Open Source Ecology ](https://opensourceecology.de/)  
Since August 2021 we have a [https://github.com/codecentric/spring-boot-admin](https://loa-spring-boot-admin.test.opensourceecology.de).
