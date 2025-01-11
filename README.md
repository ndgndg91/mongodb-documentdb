# mongodb, aws document db performance test


```
make dockerBuild MODULE=mongodb
make dockerBuild MODULE=documentdb
```


### M4, 15.2 issue
- https://github.com/corretto/corretto-21/issues/85
```
echo "-XX:UseSVE=0"
```