# Table of content
[[_TOC_]]

# Overview
This is the loa-app, that shows Actors & Events in the context of: https://linkedopenactors.org

# Run docker image
The images should be build & pushed to gitlab [regsitry](https://gitlab.com/naturzukunft.de/public/loa/loa-app/container_registry) each time the build pipeline succeed.  
  
There should be a 'latest' image per environment (environment is named oneToOne with git branch!). So for the develop environment/branch, there is:  
`registry.gitlab.com/naturzukunft.de/public/loa/loa-app:develop`  
  
Run it with:  
`docker run -p 8080:8080 registry.gitlab.com/naturzukunft.de/public/loa/loa-app:develop`

# Access the Actors & Events
**Currently (March 2021 there is no support for events!)**  
After tunning loa-app with docker, you should be able to access it via:  
  
`http://localhost:8080/<dataProvider>/<identifier>`  
  
e.g.: http://localhost:8080/kvm/cd1ac0d81679479fb85acdf59ce69a01  
  
^ this will change in the near future, we have to find a schema for data and our main deployment.

## Retriving json+jd
```
curl --location --request GET 'http://localhost:8080/kvm/cd1ac0d81679479fb85acdf59ce69a01' \
--header 'Accept: application/json+ld'
```

## Retriving turtle
```
curl --location --request GET 'http://localhost:8080/kvm/cd1ac0d81679479fb85acdf59ce69a01' \
--header 'Accept: text/turtle'
```

# KVM Adapter
The kvm adapter is located [here](https://gitlab.com/naturzukunft.de/public/loa/kvm-adapter).
- It can be used to convert a karteVonMorgen csv export in an turtle file, that can be imported in the rdf4j store.
- It's also responsible to update the rdf4j store at regular intervals. (Will be triggered by the loa-app)

# SPARQL
For querying data, you have to know which repositoryID the data provider has.  
(Also federeated repositories are possible. The have their own repositoryID.)

Here an example for the repositoryID 'kvm_loa' :

```
curl --location --request GET 'https://rdf.dev.osalliance.com/rdf4j-server/repositories/kvm?query=SELECT%20*%20WHERE%20%7B%20%3Fs%20%3Fp%20%3Fo%20.%20%7D%20LIMIT%2010' \
--header 'Accept: application/sparql-results+xml, */*;q=0.5'
```

^ the query is a little bit ugly! In this case it's `SELECT * WHERE { ?s ?p ?o . } LIMIT 10`

# Development
## start feature branch
Please use [gitflow-maven-plugin](https://github.com/aleksandr-m/gitflow-maven-plugin). As feature name, choose a gitbal issue key together with a short description.
`mvn gitflow:feature-start -DpushRemote=true`

## finish feature branch
Please use [gitflow-maven-plugin](https://github.com/aleksandr-m/gitflow-maven-plugin). Take Action to your commit messages, see [How to Write a Git Commit Message](https://chris.beams.io/posts/git-commit/)! 
`mvn gitflow:feature-finish`
