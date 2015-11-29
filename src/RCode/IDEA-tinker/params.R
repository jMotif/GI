# libs: GRAPHICS
library(ggplot2)
library(grid)
library(gridExtra)
library(scales)
library(Cairo)
# libs: DATA
library(plyr)
library(dplyr)
library(stringr)
#
#
data <- read.table("../../grammarsampler_approx.txt", skip=1, as.is=T)
data[1,]
names(data) <-
  c("dataset", "W", "P", "A", "approx", "rules", "max_freq", "cover", "coverage", 
    "pr_rules", "pr_max_freq", "pr_cover", "pr_coverage")
str(data)
head(data)
unique(data$dataset)
dd = data[data$dataset == "ann_gun_CentroidA1.txt" & data$cover > 0.98,]
dd$rule_reduction <- dd$pr_rules/dd$rules
head(arrange(dd, rule_reduction))

pi_cover <- ggplot(dd, aes(x = P, y = A)) + 
  geom_tile(aes(fill=cover), colour="grey") + scale_fill_gradientn(colours = rev(terrain.colors(100)), limits=c(0.0, 1.0)) +
  scale_x_continuous("PAA size", limits = c(6, 26), breaks=c(4, 8, 12, 16, 20, 24)) + 
  scale_y_continuous("SAX alphabet size", limits = c(3, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series cover by grammar rules") +
  theme(legend.position="bottom") 
pi_cover

pi_cover <- ggplot(dd, aes(x = P, y = A)) + 
  geom_tile(aes(fill=approx), colour="grey") + 
  scale_fill_gradientn(colours = rev(terrain.colors(100))) +
  scale_x_continuous("PAA size", limits = c(6, 26), breaks=c(4, 8, 12, 16, 20, 24)) + 
  scale_y_continuous("SAX alphabet size", limits = c(3, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series cover by grammar rules") +
  theme(legend.position="bottom") 
pi_cover

pi_cover <- ggplot(dd, aes(x = P, y = A)) + 
  geom_tile(aes(fill=pr_rules/rules), colour="grey") + 
  scale_fill_gradientn(colours = rev(terrain.colors(100))) +
  scale_x_continuous("PAA size", limits = c(6, 26), breaks=c(4, 8, 12, 16, 20, 24)) + 
  scale_y_continuous("SAX alphabet size", limits = c(3, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series cover by grammar rules") +
  theme(legend.position="bottom") 
pi_cover

pi_cover <- ggplot(dd, aes(x = P, y = A)) + 
  geom_jitter(aes(fill=pr_rules/rules), colour="grey") + 
  ggtitle("Time series coverage by grammar rules") +
  scale_shape_discrete(name = "Sliding window size") + 
  scale_size_continuous(guide = FALSE) +
  scale_color_gradientn(name = "Coverage:  ",limits=c(0,1),
    colours=c("red","orange","lightblue","darkblue"), breaks=c(0,0.5,1),
    guide = guide_colorbar(title.theme=element_text(size=14, angle=0),title.vjust=1,
    barheight=0.6, barwidth=6, label.theme=element_text(size=10, angle=0))) +
  scale_x_continuous("PAA size", limits = c(3, 25)) + 
  scale_y_continuous("SAX alphabet size", limits = c(3, 11)) + 
  theme_bw() + theme(legend.position="bottom") 
pi_cover
#
plot_series1 <- function(dd,fname){
  res=ggplot(data=dd,aes(x=timestamps,y=value))+theme_bw()+geom_line(colour="darkgrey")+
    ggtitle(paste("YAHOO EGADS,",fname))
  is_in_anomaly=0
  start=-1
  end=-1
  for(i in c(1:length(dd$anomaly))){
    v = dd$anomaly[i]
    if(v==1 && is_in_anomaly==0){
      start=i
      is_in_anomaly=1
      #print(paste("start",i))
    }
    if( (v==0 && is_in_anomaly==1) || (is_in_anomaly==1 && i==length(dd$anomaly)) ){
      end=i
      is_in_anomaly=0      
      #print(paste("end",i))
      red_line_segment=dd[(start:end),]
      red_line_segment$type="anomaly"
      res=res+geom_line(data=red_line_segment,aes(x=timestamps,y=value,color=type),size=1.5)
    }
  }
  is_in_change=0
  start=-1
  end=-1
  for(i in c(1:length(dd$changepoint))){
    v = dd$changepoint[i]
    if(v==1 && is_in_change==0){
      start=i
      is_in_change=1
      #print(paste("start",i))
    }
    if( (v==0 && is_in_change==1) || (is_in_change==1 && i==length(dd$changepoint)) ){
      end=i
      is_in_change=0      
      #print(paste("end",i))
      green_line_segment=dd[(start:end),]
      green_line_segment$type="change"
      res=res+geom_line(data=green_line_segment,aes(x=timestamps,y=value,color=type),size=1.5)
    }
  }
  res + theme(legend.position="bottom") + scale_colour_discrete(name="Type: ")  
}
#
plot_series2 <- function(dd,fname){
  res=ggplot(data=dd,aes(x=timestamps,y=value))+theme_bw()+geom_line(colour="darkgrey")+ggtitle(paste("RRA,",fname))
  res
  is_in_anomaly=0
  start=-1
  end=-1
  for(i in c(1:length(dd$rra_anomaly))){
    v = dd$rra_anomaly[i]
    if(v>0 && is_in_anomaly==0){
      start=i
      is_in_anomaly=1
      #print(paste("start",i))
    }
    if( (v==0 && is_in_anomaly==1) || (is_in_anomaly==1 && i==length(dd$rra_anomaly)) ){
      end=i
      is_in_anomaly=0      
      #print(paste("end",i))
      red_line_segment=dd[((start+1):(end)),]
      red_line_segment$rank=as.character(red_line_segment$rra_anomaly[1]-1)
      res=res+geom_line(data=red_line_segment,aes(x=timestamps,y=value,color=rank),size=1.5)
    }
  }
  res + theme(legend.position="bottom") + scale_colour_discrete(name="Anomaly rank: ")  
}
#
dd=dat
i=2
fname=f$base
for(i in c(1:length(ll$base))){
  f=ll[i,]
  print(paste(f$base))
  dat=read.table(f$base,header=T,sep=",")
  tmp=read.table(f$anomaly,header=F,sep=",")
  dat$rra_anomaly=tmp$V1
  p1<-plot_series1(dat, f$base)
  p2<-plot_series2(dat, f$base)
  CairoPNG(file = paste(f$base,'.png',sep=""),width = 900, height = 600)
  print(grid.arrange(p1, p2, ncol=1))
  dev.off()
}