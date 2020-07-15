# AndroidViewModelExtensions

## ライブラリの導入の仕方
プロジェクトのルートディレクトリで

```
git submodule init
```

```
git submodule update
```

をする。

ライブラリに変更があった場合は


```
git submodule foreach 'git pull origin master' && git submodule update
```
