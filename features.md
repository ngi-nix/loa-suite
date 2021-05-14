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

* [HTML Overview](https://loa.test.opensourceecology.de/algorithms)  
* Turtle overview  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithms'`
* Turtle Details  
`curl -H "Accept: text/turtle" --location --request GET 'http://localhost:8080/algorithm/fuzzySearchWeightedRatioAlgorithm'`

# Production helper
* [Health endpoint](https://loa.test.opensourceecology.de/actuator/health)
* [Info endpoint](https://loa.test.opensourceecology.de/actuator/info)

# Known Bugs
* Broken Links
The Links on the [HTML Overview](https://loa.test.opensourceecology.de/algorithms) are broken.   
**Workarround** use the following links:

    * [fuzzySearchTokenSortPartialRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchTokenSortPartialRatioAlgorithm)
    * [fuzzySearchTokenSortRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchTokenSortRatioAlgorithm)
    * [fuzzySearchTokenSetPartialRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchTokenSetPartialRatioAlgorithm)
    * [fuzzySearchWeightedRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchWeightedRatioAlgorithm)
    * [fuzzySearchTokenSetRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchTokenSetRatioAlgorithm)
    * [fuzzySearchPartialRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchPartialRatioAlgorithm)
    * [fuzzySearchRatioAlgorithm](https://loa.test.opensourceecology.de/algorithm/howTofuzzySearchRatioAlgorithm)
    * [distanceCalculator](https://loa.test.opensourceecology.de/algorithm/howTodistanceCalculator)
