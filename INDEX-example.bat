@ECHO OFF
java -jar target\IndexerAndSearchTool-0.0.1-SNAPSHOT-jar-with-dependencies.jar --outputcharset UTF-8 --inputcharset cp850 --mode index --indexpath %cd%/documents/index --docspath %cd%/documents/input --outpath %cd% --numberofresults 50
PAUSE