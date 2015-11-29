require(reshape)
require(plyr)
require(dplyr)
#
require(stringr)
#
require(lattice)
require(ggplot2)
require(gridExtra)
require(scales)
require(Cairo)
# The palette with grey:
cbPalette <- c("#999999", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
               "#D55E00", "#CC79A7")
# The palette with black:
cbbPalette <- c("#000000", "#E69F00", "#56B4E9", "#009E73", "#F0E442", "#0072B2", 
                "#D55E00", "#CC79A7")
# To use for fills, add
# scale_fill_manual(values=cbPalette)
# To use for line and point colors, add
# scale_colour_manual(values=cbPalette)
#
datas = read.table("../resources/sampling/ecg0606_grammarsampler.out", as.is = T, header = T)
datas[datas$frequency == -2147483648,]$frequency = NA
datas = datas[complete.cases(datas),]
datas = filter(datas, cover > 0.95)
datas$reduction = datas$pruned_rules / datas$rules
head(arrange(datas, reduction))


datar = read.table("../resources/sampling/ecg0606_repair_grammarsampler.out", as.is = T, header = T)
datar[datar$frequency == -2147483648,]$frequency = NA
datar = datar[complete.cases(datar),]
datar = filter(datar, cover > 0.95)
datar$reduction = datar$pruned_rules / datar$rules
head(arrange(datar, reduction))



datasets = unique(data$dataset)
datasets
#
data$reduction = data$pruned_rules / data$rules
str(data)
#
# correlation between approximation and the reduction
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$approximation, x$reduction), 4)
  pv <- round(cor.test(x$approximation, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  print(ll)
  p <- ggplot(x, aes(approximation, reduction)) + geom_smooth(method = "lm") + 
  geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
  theme_bw() + ggtitle(paste(unique(x$dataset))) +
  scale_x_continuous(limits = c(0.2,1.25), breaks = c(0.25, 0.5, 0.75, 1.0, 1.25)) + 
  scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
  geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
            hjust = 0, vjust = 0)
  p
})  
do.call(grid.arrange,  pl)

Cairo(width = 900, height = 700, 
      file="approx_reduction_corr.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
do.call(grid.arrange,  pl)
dev.off()


pl = dlply(data, .(dataset), function(x){
 wireframe(approximation ~ paa * alphabet, data = x[x$window<200 & x$window>100,],
          xlab = "PAA", 
          ylab = "Alphabet",
          zlab = list(label="Approx. error",rot=90),
          main = paste(unique(x$dataset)),
          #par.settings = list(#box.3d = list(col = "transparent", alpha = 0),
          #axis.line = list(col = "transparent"),
          par.settings = list(clip=list(panel=FALSE)),
          drape = TRUE,
          colorkey = TRUE,
          col.regions = rainbow(100, s = 1, v = 1, start = 0, end = max(1,100 - 1)/100, alpha = 1),
          screen = list(z = -120, x = -70)
)})
Cairo(width = 900, height = 700, 
      file="approx_params.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
grid.arrange(pl[[4]], pl[[5]], pl[[7]],
             pl[[3]], pl[[8]], pl[[10]], 
             ncol=3, nrow=2)
dev.off()



# correlation between frequency and the reduction
pl = dlply(data, .(dataset), function(x){
  if(!is.na(str_match(paste(unique(x$dataset)), "TEK"))){
    x = x[x$pruned_frequency<30,]
  }
  cor <- round(cor(x$pruned_frequency, x$reduction), 4)
  pv <- round(cor.test(x$pruned_frequency, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$reduction==mr,]$pruned_frequency[1],y=mr)
  p <- ggplot(x, aes(pruned_frequency, reduction)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous(limits = c(0,max(x$pruned_frequency))) + 
    scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=x, y=y), color="red", size=3.5)
})  
do.call(grid.arrange,  pl)

Cairo(width = 900, height = 700, 
      file="reduction_frequency_corr.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
do.call(grid.arrange,  pl)
dev.off()

# correlation between frequency and the reduction
pl = dlply(data, .(dataset), function(x){
  cor <- round(cor(x$pruned_frequency, x$reduction), 4)
  pv <- round(cor.test(x$pruned_frequency, x$reduction)$"p.value", 4)
  ll <- paste("r=", cor, "\npval=", pv, sep = "")
  mr <- min(x$reduction)
  dd <- data.frame(x=x[x$reduction==mr,]$pruned_frequency[1],y=mr)
  p <- ggplot(x, aes(pruned_frequency, reduction)) + geom_smooth(method = "lm") + 
    geom_jitter(alpha = 0.4, col = cbPalette[4], size = 0.35) + 
    theme_bw() + ggtitle(paste(unique(x$dataset))) +
    scale_x_continuous(limits = c(0,max(x$pruned_frequency))) + 
    scale_y_continuous(limits = c(0,1), breaks = c(0, 0.25, 0.5, 0.75, 1.0)) + 
    geom_text(data = NULL, label = ll, x = 0.25, y = 0.64, col = cbPalette[6], 
              hjust = 0, vjust = 0)
  p + geom_point(data=dd, aes(x=x, y=y), color="red", size=3.5)
})  
do.call(grid.arrange,  pl)

Cairo(width = 900, height = 700, 
      file="reduction_frequency_corr.pdf", type="pdf", pointsize=8, 
      bg = "transparent", canvas = "white", units = "px", dpi = 74)
do.call(grid.arrange,  pl)
dev.off()
