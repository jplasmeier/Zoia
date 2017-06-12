# Zoia

Zoia is a tool for accessing a music library via a web interface.

## Introduction 

Case Western Reserve University supplies each student with a Google Drive account that has no storage limit. Naturally, this makes Drive the best place for storing my growing music collection (~100Gb as of 05/27/2017). 

Zoia is an API for interacting with a music library stored on Google Drive, with support for other clients possible. In addition to exposing the library as a filesystem (as it is on Drive), Zoia provides more music library specific abstraction. 

For example, retrieving an album object is no longer a call to the Drive API for a folder. Rather, Zoia returns an album object, with tracks, metadata, and an image file.

Zoia also serves as a caching layer between Google Drive or a network file system, and a client application. This provides greater flexibility with serving files to clients. 

In the future, Zoia would integrate with beets and provide tagging support, etc. 

## Constraints and Challenges

Preliminary research indicates that the Google Drive API does not support streaming files. The immediate solution is to first download files into a cache on the application server (with less storage). Then, these files could be streamed as normal files. 

## Design

Zoia caches a subset of files and the set of metadata for a given Music Library on Google Drive. These files could then be streamed to a client. The metadata is used to represent the library and could be used to display the library through a client. 

### Models/Resources

#### Album

An album is an object comprising tracks, metadata, and an image. Enables caching semantics (album mode).

#### Artist

An artist is an object comprising albums, metadata, and an image. Enables caching semantics (artist mode).

## Architecture

Music files are cached in an LRU cache (though this is subject to change). 

### Caching Behavior Parameters:

#### Modes

* `artist` mode - cache all albums by that artist. Includes `album` mode by default. Prolific artists may exceed memory limits. 
* `album` mode - cache all of the songs in an album. Enabled by default.
* `playlist` mode - cache all of the songs in a playlist. Enabled by default.

#### Values

* `max-size` value - the maximum amount of memory to fill with cached files. Should be a conservative estimated as this may be slightly exceeded. 

#### API

An API will support retrieval and transfer of metadata and other resources, namely music files. The API will leverage WebSockets to allow for seamless client/server communication and streaming. 

Zoia uses SQLite3 to load metadata from the filesystem into an in-memory database. This allows quick retrieval by the API. 

The API should respond to requests for cached resources immediately with a hit or miss. Once the first song is cached, the API will open the socket for that resource to the client.  

## Implementation

Zoia is planned to be mostly written in Clojure with other languages if need be. The web parts are handled by `ring` and `liberator`. 

## Testing and Evaluation

Zoia development with emphasize lots of unit, functional, and integration tests. Additionally, close attention will be paid to performance metrics of the API and of the cache. 
