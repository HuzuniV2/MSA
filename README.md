# MSA
May papa moniz watch over us


# Agents

### Aglomerator
Get arguments such as Politics:FMI:title, or Sports:Ronaldo:Body (type:keyword:partRequested), and asks agents of that certain type,
to find and parts of news containing the specified keywords (only titles or only bodies). In the end it displays an HTML file containing all
the news given to him by the Finder agents.

### Finders
Each finder agent is bound to a domain/website (similar to the operator agents in the calculator), they are registered in the DF with a certain type, depending on the domain,
and we need them to somehow connect to the website and parse the needed html and send it to the aglomerator.


There is no need to worry abou the mobile version, he said that in the metting. Since you are not portuguese, i think its safe to assume we can use 
internation websites like cnn and skynews or whatever to get the information instead of portuguese sites like he sugests.

Types of requests:
* Tiles -> keywords must be in the title
* Content -> keywords must be in the content/body
* Theme -> keyword must identify the theme of the search , doesnt need to be in the news article [ we might need to rethink the (type:keyword:partRequested) parameters]

He sugests ussing RSS feeds to simply the agregator agent:
* (http://feeds.dn.pt/DN-Ultimas) ou versões mobile
* (http://m.dn.pt/m/home) como fonte de notícias. 

He also sugests we having an extra presenter/finder agent to present the window were the keywords are, and another to launch the browser with the answears page, whatever that means.


The Finder/presenter choice should be done by a contract net.
