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

### API

An API will support retrieval and transfer of metadata and other resources, namely music files. The API may implement streaming if performance dictates doing so. 

Zoia uses SQLite3 to load metadata from the filesystem into an in-memory database. This allows quick retrieval by the API. 

The API should respond to requests for cached resources immediately with a hit or miss. Once the first song is cached, the API will open the socket for that resource to the client.  

### Auth

In order to comply with copyright law, only I should be able to access my archival backups. Therefore, the business end of Zoia will be behind a login screen. Zoia will support multiple users, but each user must authenticate into their own Google Drive account. 

### Profile/Playlists

Since we're authenticating users, we can build some more functionality per user. This includes (at least) the all-data cache, settings and playlists. 

The all-data cache is a big big json file containing the last response from retrieving all files from Google Drive. 

Playlists must comprise a user id and one or more track ids, as well as a name and Id for the playlist. 

### Durable Storage

What will Zoia store (on disk)? 

* All metadata of the Google Drive account of each user
* All playlists for each user 

#### Schema 

**Metadata-$userid**

|zid|drive_id|artist|album|track_no|total_tracks|disc_no|disc_total|length|format|bitrate|mb\_track_id|lyrics|comments|tags|
|----|----|------|-----|--------|------------|-------|----------|------|------|---------|-----------|------|--------|----|
|23|GD-234920359|Silver Jews|American Water|1|12|1|1|298|FLAC|784kbps|iguariauiheq|NULL|NULL|["country", "male vocals", "somber", "horns"]|

```
// DESIGN DECISION - Multi-tenancy

In order to efficiently store metadata, it may be advantageous to store all track metadata for all users in one large table, and have a field for the id of users who have that track in their library. 

However, this presents some challenges. First, records may be falsely duplicated, compromising any efficiency gain of mapping multiple users to the same track object. Second, this makes retreiving the metadata for a given user much slower. I do not anticipate Zoia/bonebox requiring support for many users. 

Therefore, I am deciding to allocate a new metadata table for each user. 
```

```
// DESIGN DECISION - Id Generation

How should Zoia assign unique id's to each track? The naive approach would be to use each track's Id in Google Drive as its Id in Zoia. This is a good idea because it eliminates the process of translating from Id to Google Drive Id. 

This is potentially a bad idea for the following reasons. First, if a track is deleted from Google Drive then reuploaded, the Id will almost certainly [TODO: make this a certainty] be changed. Then, a playlist in Zoia that references that track's Id will not be able to find that track. 

So, if a Google Drive Id is not sufficient to identify a given track, what is? Under perfect conditions, all files would be tagged, including a MusicBrainz track id. 

// Resolving Ids

There must exist a function that, given a track record, can resolve a URL containing the track's audio file. This *should* play nicely with caching behavior. E.g. the resolution strategy first checks the cache for the zid, and if it's a miss, make a call to the Google Drive API with the Google Drive Id. 

// Deduplication

In order to maintain a consistent identity of a track, Zoia should be able to detect duplicates. This should be as simple as using the 3-tuple of the artist, album, and track name to identify the track. 

A clever implementation could check if the new upload is of higher bitrate and automatically replace the files with the higher quality version. However, this is low priority. 
```

In addition, there may be some use in storing extra metadata, for example, tags from rateyourmusic or user-added tags. It would be possible to create tables with tracks indexed by each tag. 

### Caching

Music files are cached in an LRU-ish cache with several modes. 

#### Modes

* `artist` mode - cache all albums by that artist. Includes `album` mode by default. Prolific artists may exceed cache space limits. 
* `album` mode - cache all of the songs in an album. Enabled by default.
* `playlist` mode - cache all of the songs in a playlist. Enabled by default.

#### Values/Parameters

* `max-size` value - the maximum amount of memory to fill with cached files. Should be a conservative estimated as this may be slightly exceeded. 

## Implementation

Zoia is planned to be mostly written in Clojure with other languages if need be. The web parts are handled by `ring` and `liberator`. 

Rough Plan:

|Task|Time|Notes|Acceptance Criteria|
|----|----|-----|-------------------|
|Call Google Drive Java API from Clojure| 3 hours | ||
|Implement auth, user sessions/profiles, etc.| 5 - 8 hours | Hopefully there's a framework or existing project to fork|Login page with user accounts (accnt mgmt can be hard coded), load some content based on user session info|
|Retrieve JSON from Google Drive | 2 - 3 hours | Shouldn't be too bad, unless I have too many tracks for 1 request...| Send a request to `/all` and receive JSON of all Google Drive tracks.|
|Populate database with metadata from Google Drive.| 5 - 7 hours | This might take awhile to get right and will be tricky to test. Definitely should start with a subset of my library before attempting to cache all of it. | Asynchronous process to load db, Db contains consistent/sound/correct records of each track including a Zid. | 
|Retrieve cached track metadata when possible | 2 - 3 hours |Background loading may be tricky...| Response from Drive API call is saved, and loaded on next log in. Updates in the background. |
|Resolve files on Drive | 1 - 2 hours | Shouldn't be too bad | Using the same UI, double-clicking on a track opens a connection to the Google Drive file containing the audio of the track. No caching.|
|Cache audio files | 5 - 8 hours | The heart of Zoia! | Playing a song fires an asynchronous process which downloads the file to the Zoia host and puts it in a cache. Upon playing the song again, the local file is used instead of Google Drive.|  


## Testing and Evaluation

Zoia development with emphasize lots of unit, functional, and integration tests. Additionally, close attention will be paid to performance metrics of the API and of the cache. 
