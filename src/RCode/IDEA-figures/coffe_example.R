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
# The palette with black:
cbbPalette <- c("#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
                "#D55E00", "#CC79A7")

#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1]
dd <- data.frame(x = 1:length(unlist(cd)), y = unlist(cd))
p = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=0.7) + 
  scale_y_continuous(limits=c(-1,35)) +
  theme_bw() + ggtitle(paste("An illustration of time series subsequence extraction\n",
                      "via sliding window")) +
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
cd_df$color=c("brown1","brown1","brown")
row.names(cd_df)=NULL
cd_m = melt(cd_df, id.var=c("window","color"))
vv=(c(10,15,20))
dv = vv
for(i in c(1:step)){
  dv = c(dv, vv+i)
}
cd_m$variable = dv
#
sg=data.frame(x=c(10,15,20,c(10,15,20)+step),
              y=c(dd$y[c(10,15,20,c(10,15,20)+step)]),
              yend=c(2,1,0,2,1,0))
sf=data.frame(x=c(10,15,20),xend=c(10,15,20)+step,
              y=c(2,1,0),yend=c(2,1,0),color=c("brown1","brown1","brown"))

p = 
p + geom_segment(data=sg,aes(x=x,y=y,xend=x,yend=yend), col="brown", lty=3, lwd=0.5) +
  geom_path(data=cd_m,aes(x=variable,y=value,group=window),color=cd_m$color,lwd=0.8) +
  geom_segment(data=sf,aes(x=x,y=y,xend=xend,yend=yend), color=sf$color, lwd=1)
p
#
#
#
#
#
#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1][70:170]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p2 = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=0.7) + 
  theme_bw() + ggtitle(paste("An example of SAX transform: a time series of 100 points\n",
                "converted into 5 letters, 'cabbc'")) +
  scale_x_continuous(limits=c(-40,105)) + scale_y_continuous(limits=c(-2,2)) +
  theme(axis.title=element_blank(), legend.position="none")
p2
#
segments = 5
inc = length(dd$y)/segments # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p2 + geom_vline(xintercept=breaks, lty=3, color="red", lwd=0.5)
#
paa_points = data.frame(paa = jmotif::paa(dd$y, segments), 
                 paa_centers = (breaks + (inc / 2))[1:segments])
paa_segments = data.frame(x=breaks[1:segments],y=paa_points$paa,
  xend=breaks[2:(segments+1)],yend=paa_points$paa,
  color=c(cbbPalette[4],cbbPalette[6],cbbPalette[7],cbbPalette[7],cbbPalette[4]))
p2 = p2 + geom_vline(xintercept=breaks, lty=3, color="red",lwd=0.5) + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=3.7, color=paa_segments$color) +
 geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend), 
              color=paa_segments$color,lwd=1)
p2
bell = data.frame(y = seq(-2,2, length=100))
bell$x= dnorm(bell$y, mean=0, sd=1)
bell=arrange(bell,y)
bell$order = 1:100
cuts = data.frame(y=alphabet_to_cuts(3)[2:3])
labels=data.frame(label=c("a","b","c"),x=rep(-12,3),y=c(-1,0,1))
p2 = p2 + geom_path(data=bell,aes(x=x*50-40,y=y),col="magenta",order=order,lwd=1.2) +
  geom_hline(data=cuts,aes(yintercept=y), lty=5, lwd=0.8, col="magenta") +
  geom_text(data=labels,aes(x=x,y=y,label=label),
            color=c(cbbPalette[6],cbbPalette[7],cbbPalette[4]), size=8)
p2
#
#
#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1][70:170]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p3 = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=0.7) + 
  theme_bw() + ggtitle(paste("An example of the PAA approximation error computation as\n",
    "the sum of distances between points and their PAA values")) +
  scale_x_continuous(limits=c(0,105)) + scale_y_continuous(limits=c(-2,2)) +
  theme(axis.title=element_blank(), legend.position="none")
p3
#
segments = 5
inc = length(dd$y)/segments # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p3 + geom_vline(xintercept=breaks, lty=3, color="red")
#
paa_points = data.frame(paa = jmotif::paa(dd$y, segments), 
                        paa_centers = (breaks + (inc / 2))[1:segments])
paa_segments = data.frame(x=breaks[1:segments],y=paa_points$paa,
                          xend=breaks[2:(segments+1)],yend=paa_points$paa)
p3 = p3 + geom_vline(xintercept=breaks, lty=3, color="red",lwd=0.5) + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=2.7, col="brown") +
  geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend), color="brown",lwd=1)
p3
#
x1 <- filter(dd, x<=breaks[2])$x
df=data.frame(
  x=x1, xend=x1, y=dd$y[x1], yend=rep(paa_points$paa[1],length(x1)))
p3=p3+geom_segment(data=df,aes(x=x,y=y,yend=yend,xend=xend),col="cyan")
#
x2 <- filter(dd, x>breaks[2], x<=breaks[3])$x
df=data.frame(
  x=x2, xend=x2, y=dd$y[x2], yend=rep(paa_points$paa[2],length(x2)))
p3=p3+geom_segment(data=df,aes(x=x,y=y,yend=yend,xend=xend),col="cyan")
#
x3 <- filter(dd, x>breaks[3], x<=breaks[4])$x
df=data.frame(
  x=x3, xend=x3, y=dd$y[x3], yend=rep(paa_points$paa[3],length(x3)))
p3=p3+geom_segment(data=df,aes(x=x,y=y,yend=yend,xend=xend),col="cyan")
#
x4 <- filter(dd, x>breaks[4], x<=breaks[5])$x
df=data.frame(
  x=x4, xend=x4, y=dd$y[x4], yend=rep(paa_points$paa[4],length(x4)))
p3=p3+geom_segment(data=df,aes(x=x,y=y,yend=yend,xend=xend),col="cyan")
#
x5 <- filter(dd, x>breaks[5], x<=breaks[6])$x
df=data.frame(
  x=x5, xend=x5, y=dd$y[x5], yend=rep(paa_points$paa[5],length(x4)))
p3=p3+geom_segment(data=df,aes(x=x,y=y,yend=yend,xend=xend),col="cyan")
p3 = p3 + geom_line(color = "cornflowerblue", lwd=0.7) + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=2.7, col="brown") +
  geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend), color="brown",lwd=1)
#
#
#
#
#
cd <- read.table("../resources/Coffee/Coffee_TEST",as.is=T)[12,-1][70:170]
dd <- data.frame(x = 1:length(unlist(cd)), y = znorm(unlist(cd)))
p4 = ggplot(dd, aes(x = x, y = y)) + geom_line(color = "cornflowerblue", lwd=0.7) + 
  theme_bw() + ggtitle(paste("An example of the Alphabet error computation as the sum\n",
        "of dist. between PAA and their alphabet cut segment centers")) +
  scale_x_continuous(limits=c(0,105)) + scale_y_continuous(limits=c(-2,2)) +
  theme(axis.title=element_blank(), legend.position="none")
p4
#
segments = 5
inc = length(dd$y)/segments # there shall be 15 segments
breaks = seq(1-0.5,length(dd$y)+0.5,by = inc)
p4 + geom_vline(xintercept=breaks, lty=3, color="red")
#
paa_points = data.frame(paa = jmotif::paa(dd$y, segments), 
                paa_centers = (breaks + (inc / 2))[1:segments])
paa_segments = data.frame(x=breaks[1:segments],y=paa_points$paa,
                xend=breaks[2:(segments+1)],yend=paa_points$paa,
                color=c(cbbPalette[4],cbbPalette[6],cbbPalette[7],cbbPalette[7],cbbPalette[4]))
p4 = p4 + geom_vline(xintercept=breaks, lty=3, color="red") + 
  geom_point(data=paa_points, aes(x=paa_centers, y=paa), pch=9, cex=2.7, col="brown") +
  geom_segment(data=paa_segments, aes(x=x,y=y,xend=xend,yend=yend),color=paa_segments$color,lwd=1.2)
p4
#
cuts = data.frame(y=alphabet_to_cuts(3)[2:3])
cuts_c = data.frame(y=c(-0.967421566101701, 0, 0.967421566101701))
dseg = data.frame(x=paa_points$paa_centers,xend=paa_points$paa_centers,
                  y=c(cuts_c$y[3],cuts_c$y[1],cuts_c$y[2],cuts_c$y[2],cuts_c$y[3]), 
                  yend=paa_points$paa)
p4 = p4 + geom_hline(data=cuts,aes(yintercept=y), lty=5, lwd=0.8, col="magenta") +
  geom_hline(data=cuts_c,aes(yintercept=y), lty=2, lwd=0.6, col="magenta") +
  geom_segment(data=dseg,aes(x=x,y=y,xend=xend,yend=yend),col="cyan",lwd=1.5)
p4


Cairo(width = 900, height = 500, 
      file="sax.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 70)
grid.arrange(p,p3,p2,p4,ncol=2)
dev.off()
