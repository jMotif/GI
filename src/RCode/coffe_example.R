library(jmotif)
# libs
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
library(reshape)
library(plyr)
library(dplyr)
#
cd <- read.table("Coffee/Coffee_TEST")[2,-1]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue") + theme_bw() +
  ggtitle("An instant Coffee spectroghram as a time series ") +
  scale_x_continuous(limits=c(-40,300))
p
#
inc = length(dd$y)/9 # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p + geom_vline(xintercept=breaks, lty=3, color="red")
#
paa9 = data.frame(paa = jmotif::paa(dd$y, 9), paa_centers = (breaks + (inc / 2))[1:9])
points(x=paa_centers, y=paa7, pch=9, cex=0.7, col="brown")
segments(x0=breaks[1:7],x1=breaks[2:8],y0=paa7,y1=paa7,col="brown")

p + geom_vline(xintercept=breaks, lty=3, color="red") + 
  geom_point(data=paa9, aes(x=paa_centers, y=paa, color = "red")) + 
  segments(x0=breaks[1:7],x1=breaks[2:8],y0=paa7,y1=paa7,col="brown")

bell = data.frame(y = seq(-2,2, length=100))
bell$x= dnorm(bell$y, mean=0, sd=1)
bell=arrange(bell,y)
bell$order = 1:100
p + geom_path(data=bell,aes(x=x*60-30,y=y,col="magenta",order=order))

abline(h = alphabet_to_cuts(3)[2:3], lty=2, lwd=2, col="magenta")
text(0.7,-1,"a",cex=2,col="magenta")
text(0.7, 0,"b",cex=2,col="magenta")
text(0.7, 1,"c",cex=2,col="magenta")
