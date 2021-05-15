# Startpage
On the [startpage](https://loa.test.opensourceecology.de/) you find some links.

# Adapter 
## Karte von Morgen
[kvm sync runs every minute](https://linkedopenactors.org/#karte-von-morgen-kvm)  
Last sync timestamp is logged [here](https://loa.test.opensourceecology.de/lastSync)
## WECHANGE
[WECHAMGE sync runs every minute](https://linkedopenactors.org/#wechange)  
Last sync timestamp is logged [here](https://loa.test.opensourceecology.de/lastSync)

# Algorithms
You are able to see an overview of all available algorithms. By clicking on the name of an algorithm on the overview you reach the howTo page! And get further information how you can use the algorithm. Normally there is a 'Sample call' headline, which shows you how you can use/test the algorithm with the browser!

So we fullfill the requirmenst of a [loa-algorithm-provider](https://linkedopenactors.gitlab.io/loa-specification/#loa-algorithm-provider)

See also [merging-loa-organisations](https://linkedopenactors.gitlab.io/loa-specification/#merging-loa-organisations)

* [HTML Overview](https://loa.test.opensourceecology.de/algorithms)  
* Turtle overview  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithms'`
* Turtle Details  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithm/fuzzySearchWeightedRatioAlgorithm'`

# SPARQL
* https://rdf.dev.osalliance.com/rdf4j-workbench/repositories/kvm_loa/query
* https://rdf.dev.osalliance.com/rdf4j-workbench/repositories/weChange_loa/query

# Production helper
* [Health endpoint](https://loa.test.opensourceecology.de/actuator/health)
* [Info endpoint](https://loa.test.opensourceecology.de/actuator/info)
