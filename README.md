# dsd
Distributed Search and Download (CS4262 Distributed Systems - Assignment)

## Dependencies 
Have JDK 1.8+ installed.  
Have maven installed.  
Run `mvn clean install` here.  
Run `java -jar <name of the created jar file> <...arguments if any>`.  

## Arguments
`--help`, `-h`: display help.  
`--dev`: enable developer mode (loopback NI).  
`-p <port>`: port to start peer.  
`-bs <host>:<port>` address with port where the bootstrap server is.  
If these are not provided, some values will be assumed and others will have to be inserted interactively.  

## Interactive commands
`:rt`, `:routing`, `:routing-table`: display neighbors.  
`:files`, `:own-files`: display hosted files in the current peer.  
`:exit`: gracefully exit.  
Use anything without a colon prefix to search in the network.  
