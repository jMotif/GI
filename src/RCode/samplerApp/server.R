library(ggplot2)
library(dplyr)

data=read.csv(gzfile("rules_num.txt.gz"))
#names(data) <- c("window","paa","alphabet","size","approx_dist")

# Define server logic required to generate and plot a random distribution
shinyServer(function(input, output) {
  
  # Expression that generates a plot of the distribution. The expression
  # is wrapped in a call to renderPlot to indicate that:
  #
  #  1) It is "reactive" and therefore should be automatically 
  #     re-executed when inputs change
  #  2) Its output type is a plot 
  #
  output$samplesPlot <- renderPlot({
    
    aRange <- input$aSizeRange
    windowRange <- input$winSizeRange
    paaRange <- input$paaSizeRange
    distRange <- input$approxDistanceRange
    gSizeRange <- input$compressedGrammarSizeRange
    
    p=ggplot(filter(data, 
                    window>=windowRange[1], window<=windowRange[2], 
                    paa>=paaRange[1], paa<=paaRange[2], 
                    alphabet>=aRange[1], alphabet<=aRange[2], 
                    approx_dist>=distRange[1], approx_dist<=distRange[2],
                    compressedsize>=gSizeRange[1], compressedsize<=gSizeRange[2]
                    ),
             aes(x=compressedsize,y=approx_dist,color=factor(paa),shape=factor(alphabet),size=factor(window))) + 
      scale_x_continuous("Pruned Sequitur grammar size") + scale_y_continuous("SAX transform approximation distance") +
      geom_point(alpha=0.9) + guides(color=guide_legend(ncol=4,override.aes=list(size=5,alpha=1))) + 
      guides(shape=guide_legend(ncol=2),override.aes=list(size=4)) + guides(size=guide_legend(ncol=3)) +
      theme_bw() + scale_shape_manual(values=seq(0,10))
    print(p)
  })
})