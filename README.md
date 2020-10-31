# PIETRON

[![Netlify Status](https://api.netlify.com/api/v1/badges/00a3482b-d90b-40c5-bceb-261d36f64658/deploy-status)](https://app.netlify.com/sites/pietron/deploys)

## Setup And Run

#### Install dependencies
```shell
npm install
```

#### Run dev server

```shell
npm run dev
```

Good idea to re-run this if you later encounter misbehavior in the repl,
like unusual warnings etc or not evaluating code.

Troubleshooting: Make sure there are no conflicting release build present.

#### Start repl with server

For shell repl with dev server running:

```shell
npm run repl
```

Alternatively:

In atom it is ^d   ^,l
Stop the dev server first if it is running?


#### Compile an optimized version

Exit the repl (if any) to avoid conflict.
Do `npm run clean` to avoid garbage from dev in release.
`npm install`
Then:

```shell
npm run release
```
