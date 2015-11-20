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
data <- read.table("../../grammarsampler.txt", header = T, as.is = T)
head(data)
str(data)
head(data)
unique(data$dataset)

data$rule_reduction <- data$pruned_rules/data$rules

dd = data[data$cover > 0.98,]
str(dd)

ddply(dd, c("dataset"), summarize,
  opt = min(rule_reduction)
  )

head(arrange(dd, rule_reduction))
qqplot(dd$rules, dd$approximation)
qqplot(dd$rule_reduction^4, dd$approximation)
qqline(dd$rule_reduction, (dd$approximation)^(1/32))

qqexp <-  function(y, line=FALSE, ...) { 
  y <- y[!is.na(y)]
  n <- length(y)
  x <- qexp(c(1:n)/(n+1))
  m <- mean(y)
  if (any(range(y)<0)) stop("Data contains negative values")
  ylim <- c(0,max(y))
  qqplot(x, y, xlab="Exponential plotting position",ylim=ylim,ylab="Ordered sample", ...)
  if (line) abline(0,m,lty=2)
  invisible()
}
qqexp(dd$approximation, line=T)




dd[dd$rule_reduction==min(dd$rule_reduction),]
par(mfrow=c(1,1))
hist(dd$rule_reduction)
hist(dd$approximation)

qqplot(dd$rule_reduction, dd$approximation)
points(min(dd$rule_reduction), dd[dd$rule_reduction==min(dd$rule_reduction),]$approximation,col="red",lwd=10)


pi_cover1 <- ggplot(data, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=cover), colour="grey") + scale_fill_gradientn(colours = rev(terrain.colors(100)), limits=c(0.0, 1.0)) +
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series span cover by grammar rules") +
  theme(legend.position="bottom") 
pi_cover1


pi_cover2 <- ggplot(dd, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=cover), colour="grey") + scale_fill_gradientn(colours = rev(terrain.colors(100)), limits=c(0.0, 1.0)) +
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series span cover by grammar rules, threshold>0.95") +
  theme(legend.position="bottom") 
pi_cover2

CairoPDF(width = 12, height = 6, file = "video_span_cover.pdf", bg = "transparent")
print(grid.arrange(pi_cover1, pi_cover2, layout_matrix = cbind(c(1,1), c(2,2))))
dev.off()
#
#
#
#
pi_approx1 <- ggplot(data, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=approximation), colour="grey") + 
  scale_fill_gradientn(colours = terrain.colors(100), limits=range(data$approximation)) +
  
  geom_jitter(aes(color=approximation)) + 
  scale_color_gradientn(colours = terrain.colors(100), limits=range(data$approximation)) +
  
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series approximation precision") +
  theme(legend.position="bottom") 
pi_approx1

pi_approx2 <- ggplot(dd, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=approximation), colour="grey") +
  scale_fill_gradientn(colours = terrain.colors(100), limits=range(dd$approximation)) +
  
  geom_jitter(aes(color=approximation)) + 
  scale_color_gradientn(colours = terrain.colors(100), limits=range(dd$approximation)) +
  
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Time series approximation precision, cover>0.95") +
  theme(legend.position="bottom") 
pi_approx2

CairoPDF(width = 12, height = 6, file = "video_approx_dist2.pdf", bg = "transparent")
print(grid.arrange(pi_approx1, pi_approx2, layout_matrix = cbind(c(1,1), c(2,2))))
dev.off()
#
#
#
#
pi_approx1 <- ggplot(dd, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=pruned_rules), colour="grey") + 
  scale_fill_gradientn(colours = terrain.colors(100), limits=range(data$pruned_rules)) +
  
  geom_jitter(aes(color=pruned_rules)) + 
  scale_color_gradientn(colours = terrain.colors(100), limits=range(data$pruned_rules)) +
  
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Rules after pruning, cover threshold >0.95") +
  theme(legend.position="bottom") 
pi_approx1

pi_approx2 <- ggplot(dd, aes(x = paa, y = alphabet)) + 
  geom_tile(aes(fill=rule_reduction), colour="grey") +
  scale_fill_gradientn(colours = terrain.colors(100), limits=range(dd$rule_reduction)) +
  
  geom_jitter(aes(color=rule_reduction)) + 
  scale_color_gradientn(colours = terrain.colors(100), limits=range(dd$rule_reduction)) +
  
  scale_x_continuous("PAA size", limits = c(1, 31), breaks = seq(2,30,by=2)) + 
  scale_y_continuous("SAX alphabet size", limits = c(1, 11), breaks=c(2, 4, 6, 8, 10)) + 
  theme_bw() + ggtitle("Rule reduction, cover threshold >0.95") +
  theme(legend.position="bottom") 
pi_approx2

CairoPDF(width = 12, height = 6, file = "video_rule_reduction.pdf", bg = "transparent")
print(grid.arrange(pi_approx1, pi_approx2, layout_matrix = cbind(c(1,1), c(2,2))))
dev.off()
