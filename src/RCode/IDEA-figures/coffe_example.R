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
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1]
dd <- data.frame(x = 1:length(unlist(cd)), y = unlist(cd))
p = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=1) + 
  theme_bw() + ggtitle(paste("An instant Coffee spectrogram represented as a time series\n",
    "and subsequence extraction via sliding window illustration")) +
  theme(axis.title=element_blank(), legend.position="none")
p
#
#
step=40
cd_df = unlist(cd)[10:(10+step)]-1
for (i in c(15,20)) {
  cd_df=rbind(cd_df, unlist(cd)[i:(i+(step))]-(i/5-2)*2)
}
cd_df = as.data.frame(cd_df)
cd_df$window = paste(c(1:3))
row.names(cd_df)=NULL
cd_m = melt(cd_df, id.var=c("window"))
vv=(c(10,15,20))
dv = vv
for(i in c(1:step)){
  dv = c(dv, vv+i)
}
cd_m$variable = dv
#
sg=data.frame(x=c(10,15,20,c(10,15,20)+step),
              y=c(dd$y[c(10,15,20,c(10,15,20)+step)])-c(0,2,4,0,2,4),
              yend=rep(0,6),line=c(1,2,3))


p = p + 
  geom_segment(data=sg,aes(x=x,y=y,xend=x,yend=yend), col="brown", lty=3, lwd=0.5) +
  geom_path(data=cd_m,aes(x=variable,y=value,col=window,group=window),lwd=1)
#
#
#
#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1][70:170]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p2 = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=1) + 
  theme_bw() + ggtitle(paste("An example of SAX transform: a time series\n",
                             "of length 100 converted into 8 letters")) +
  scale_x_continuous(limits=c(-40,105)) + scale_y_continuous(limits=c(-2,2)) +
  theme(axis.title=element_blank(), legend.position="none")
p2
#
segments = 8
inc = length(dd$y)/segments # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p2 + geom_vline(xintercept=breaks, lty=3, color="red")
#
paa_points = data.frame(paa = jmotif::paa(dd$y, segments), 
                 paa_centers = (breaks + (inc / 2))[1:segments])
paa_segments = data.frame(x=breaks[1:segments],y=paa_points$paa,
  xend=breaks[2:(segments+1)],yend=paa_points$paa,
  color=c("blue","red","green","green","red","green","blue","red"))
p2 = p2 + geom_vline(xintercept=breaks, lty=3, color="red") + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=2.7, col="brown") +
 geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend, color=color),lwd=3)
p2
bell = data.frame(y = seq(-2,2, length=100))
bell$x= dnorm(bell$y, mean=0, sd=1)
bell=arrange(bell,y)
bell$order = 1:100
cuts = data.frame(y=alphabet_to_cuts(3)[2:3])
labels=data.frame(label=c("a","b","c"),x=rep(-12,3),y=c(-1,0,1))
p2 = p2 + geom_path(data=bell,aes(x=x*60-40,y=y),col="magenta",order=order,lwd=2) +
  geom_hline(data=cuts,aes(yintercept=y), lty=2, lwd=0.8, col="magenta") +
  geom_text(data=labels,aes(x=x,y=y,label=label),color="magenta")
#
#
#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1][70:170]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p3 = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=1) + 
  theme_bw() + ggtitle(paste("An example of SAX transform: a time series\n",
                             "of length 100 converted into 8 letters")) +
  scale_x_continuous(limits=c(0,105)) + scale_y_continuous(limits=c(-2,2)) +
  theme(axis.title=element_blank(), legend.position="none")
p3
#
segments = 8
inc = length(dd$y)/segments # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p3 + geom_vline(xintercept=breaks, lty=3, color="red")
#
paa_points = data.frame(paa = jmotif::paa(dd$y, segments), 
                        paa_centers = (breaks + (inc / 2))[1:segments])
paa_segments = data.frame(x=breaks[1:segments],y=paa_points$paa,
                          xend=breaks[2:(segments+1)],yend=paa_points$paa)
p3 = p3 + geom_vline(xintercept=breaks, lty=3, color="red") + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=2.7, col="brown") +
  geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend), color="brown",lwd=2)
p3

df=data.frame(
  y=c(paa_points$paa[1], filter(dd,y>paa_points$paa[1],x<20)$y),
  x=c(0,filter(dd,y>paa_points$paa[1],x<20)$x)
)
p3=p3+geom_polygon(data=df)
df=data.frame(
  y=c(paa_points$paa[1], filter(dd,y<paa_points$paa[1],x<breaks[2])$y,paa_points$paa[1]),
  x=c(paa_points$paa_centers[1],filter(dd,y<paa_points$paa[1],x<=breaks[2])$x,breaks[2])
)
p3+geom_polygon(data=df)



Cairo(width = 900, height = 600, 
      file="sax.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(p,p2,p,p2,ncol=2)
dev.off()
