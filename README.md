# LocationSync

## Requirements

* BungeeCord
    * [Proton](https://proton-1.gitbook.io/proton/)
    * [Protocolize](https://github.com/Exceptionflug/protocolize)
* Paper Server (might work with Spigot, but haven't tested and as the Paper library is used it is probably not working)

## Configuration

### BungeeCord (Proxy)

`config.yaml` File:
```yaml
locationSync:
  # Takes a list of regex patterns to enable the location sync for the server(s)
  enabledServers: []
  # - lobby-.+
```



### Paper Server

No config options.
