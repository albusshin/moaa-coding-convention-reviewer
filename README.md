# Reviewer

Code reviewer for MOAA by decided coding conventions

## Installation

First, install [leiningen][1]

    $ git clone http://github.com/albusshin/Reviewer
    $ mv Reviewer reviewer
    $ cd reviewer
    $ lein test
    $ lein run -- --help

           usage: lein run [repo-dir] [commit|branch]

           repo-dir      : the directory of repository to be checked.
                           Checking current directory, just trivially type `.`
                           {default value: current working directory}

           commit|branch : the commit hash or the branch name
                           as the base of git diff
                           {default value: \"origin/master\"}
## License

Copyright © 2014 Albus Shin

Distributed under the Eclipse Public License version 1.0


[1]: https://github.com/technomancy/leiningen
