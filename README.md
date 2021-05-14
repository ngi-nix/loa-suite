[[_TOC_]]

# loa-suite
This is the linked-open-actors () main project. A so called Maven-Multi-Module project containing all loa projects.

# Project structure
![project_structure](doc/img/project_structure.svg "project_structure")

- loa-suite
    - loa-app
        - loa-app-controller
        - loa-app-spring-boot
    - loa-adapters
        - [loa-kvm-adapter](loa-adapters/loa-kvm-adapter/README.md)  
        - [loa-wechange-adapter](loa-adapters/loa-wechange-adapter/README.md)  
    - loa-algorithms
        - loa-algorithm
        - loa-fuzzySearchAlgorithms
        - loa-distanceCalculator
    - loa-repository

# Starting loa-app with docker image
The loa-app is a spring-boot app and it can be satrted as spring-boot app. The most users maybe prefer to use the generated docker image, wich is available in the [gitlab docker registry](https://gitlab.com/linkedopenactors/loa-suite/container_registry/1865362).

There is a latest image of each git-branch. Normally you should use the latest image from master branch!

`docker run -p8080:8080 registry.gitlab.com/linkedopenactors/loa-suite:master`

After the image is started, you can access the app: http://localhost:8080/

**Getting a list of algorithms:**  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithms'`

**Getting detail of a algorithm:**  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithm/fuzzySearchWeightedRatioAlgorithm'`



# Development Environment
Currently there is one instance running, that is public available.  
This is our [development instance](https://loa.test.opensourceecology.de/), that is provided by [Makers4Humanity](https://www.m4h.network/) & [Open Source Ecology ](https://opensourceecology.de/)
