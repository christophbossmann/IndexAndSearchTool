# Index And Search Tool

This tool intends to provide search functionality on a set of files in an efficient and fast way by creating an index as multiple files which can be stored anywhere. 

The tool can be used for instance on an encrypted drive such as Cryptomator drives. The index is also stored within the encrypted volume so it is also encrypted. When decrypted, in contrast to Operating Systems such as Windows that still don't provide searching functionality on these drives, this tool does. The decrypted index enables searching on files stored on this drive. 

## Build

 1. Make sure that maven is installed.
 2. Call `mvn clean install assembly:single`.

### Windows 

 1. Make sure that maven is installed. 
 2. Run batch script `build-windows.bat`.

## Run

### Windows
#### Index

 1. Edit `INDEX-example.bat` and adjust paths accordingly to fit path of documents to be indexed and desired path to store index and output (result).
 2. Run `INDEX-example.bat` to start indexing process.

#### Search
 1. Edit `SEARCH-example_ask_query.bat` and adjust paths accordingly to fit path of index and other parameters.
 2. Run `SEARCH-example.bat` to start search process. The tool asks for a search query in advance so you can type in any string or lucene query you intend to search for.
 
## Sample Data Collections:

Find sample pdf data collection here: https://www.loc.gov/item/2020445568/
Find sample txt data collection here: https://www.kaggle.com/datasets/jensenbaxter/10dataset-text-document-classification
