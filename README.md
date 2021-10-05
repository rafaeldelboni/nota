# Stasis
Static Markdown Blog/Site using Fulcro &amp; Pathom with no backend

# Developers

## Install dependencies
```bash
npm install
```

## Set configurations
Set the [`src/config.edn`](https://github.com/rafaeldelboni/stasis/blob/main/src/config.edn) with your keys or the corresponding enviroment variables.  

## Commands

### Local build
Start shadow-cljs watching and serving main in [`localhost:8000`](http://localhost:8000)
```bash
npm run watch
```

### Tests
Start shadow-cljs watching and serving tests in [`localhost:8022`](http://localhost:8022)
```bash
npm run watch:tests
```

Run **Karma** tests targeted for running CI tests with *Headless Chrome Driver*
```bash
npm run ci-tests
```

### Deploy
Build the release package to production deploy
```bash
npm run release
```

# License
This is free and unencumbered software released into the public domain.  
For more information, please refer to <http://unlicense.org>
