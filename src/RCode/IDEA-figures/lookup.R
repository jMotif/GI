require(reshape)
require(plyr)
require(dplyr)
require(data.table)
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
data_s = fread(input = "zcat ../resources/IDEA-sampling/sampler_sequitur.out.gz")
data_s[data_s$frequency == -2147483648,]$frequency = 0
data_s = data_s[complete.cases(data_s),]
str(data_s)

unique(data_s$dataset)

filter(data_s, dataset=="gps_track", window==330, paa==14, alphabet==4)
