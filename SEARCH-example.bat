@ECHO OFF
java -jar target\IndexerAndSearchTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --mode search --indexpath %cd%/documents/index --outpath %cd% --numberofresults 50 --query "tüv"
PAUSE