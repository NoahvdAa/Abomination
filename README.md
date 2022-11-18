# Abomination

Have you ever wanted to create a [Paper](https://github.com/PaperMC/Paper/) fork that has multiple Paper forks as its upstream?
Me neither. But thanks to this absolute trainwreck created by someone who just used Kotlin and advanced-ish gradle for the first time you can!

The system supports creating upstream-specific patches. They live in the `patches/<upstream>-api` and `patches/<upstream>-server` folders.
When applying patches, all upstream-specific patches for your upstream will be copied into the regular patch folders.
When rebuilding patches, they will be moved back to their own directories.
To create an upstream-specific patch, simply start the title with the name of the upstream, e.g. `Purpur - Remove flying squid`.

To switch upstreams, change the upstream in [gradle.properties](gradle.properties).
