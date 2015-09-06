library(ggplot2)
library(plyr)
library(dplyr)

data=read.table("paperworks/tmp.txt",header=F,sep=",")
head(data)
dd=data.frame(x=data$V6-data$V5,y=data$V4)
plot(dd)

foo=density(data$V8)
plot(foo$x, foo$y, t='l')

foo=density(data$V9)
plot(foo$x, foo$y, t='l')

df=data.frame(data$V8,data$V9)
dd=melt(df)
p ,- density(data)

wireframe(z, scales=list(arrows=FALSE), xlab='x', ylab='y', drape=TRUE,
          col.regions=terrain.colors(10), at=seq(min(z), max(z), len=11))