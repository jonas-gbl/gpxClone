<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0" xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml"
  xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd">
  <NamedLayer>
    <Name>Unwire Cph Tickets</Name>
    <UserStyle>
      <Name>tickets</Name>
      <Title>Tickets in CPH area</Title>
      <Abstract>Display of Unwire Tickets</Abstract>
      <FeatureTypeStyle>
        <Rule>
          <Title>&lt; 33</Title>
          <ogc:Filter>
            <ogc:PropertyIsLessThan>
             <ogc:PropertyName>tickets</ogc:PropertyName>
             <ogc:Literal>33</ogc:Literal>
            </ogc:PropertyIsLessThan>
          </ogc:Filter>
          <PolygonSymbolizer>
             <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#708090</CssParameter>
                <CssParameter name="fill-opacity">0.3</CssParameter>
             </Fill>     
          </PolygonSymbolizer>
        </Rule>
        <Rule>
          <Title>33 - 66</Title>
          <ogc:Filter>
            <ogc:PropertyIsBetween>
              <ogc:PropertyName>tickets</ogc:PropertyName>
              <ogc:LowerBoundary>
                <ogc:Literal>33</ogc:Literal>
              </ogc:LowerBoundary>
              <ogc:UpperBoundary>
                <ogc:Literal>66</ogc:Literal>
              </ogc:UpperBoundary>
            </ogc:PropertyIsBetween>
          </ogc:Filter>
          <PolygonSymbolizer>
             <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#FFD700</CssParameter>
                <CssParameter name="fill-opacity">0.3</CssParameter>
             </Fill>     
          </PolygonSymbolizer>
        </Rule>
        <Rule>
          <Title>&gt; 66</Title>
          <!-- like a linesymbolizer but with a fill too -->
          <ogc:Filter>
            <ogc:PropertyIsGreaterThan>
             <ogc:PropertyName>tickets</ogc:PropertyName>
             <ogc:Literal>66</ogc:Literal>
            </ogc:PropertyIsGreaterThan>
          </ogc:Filter>
          <PolygonSymbolizer>
             <Fill>
                <!-- CssParameters allowed are fill (the color) and fill-opacity -->
                <CssParameter name="fill">#32CD32</CssParameter>
                <CssParameter name="fill-opacity">0.3</CssParameter>
             </Fill>     
          </PolygonSymbolizer>
        </Rule>
        <!--<Rule>
          <Title>Boundary</Title>
          <LineSymbolizer>
            <Stroke>
              <CssParameter name="stroke-width">0.2</CssParameter>
            </Stroke>
          </LineSymbolizer>
          <TextSymbolizer>
            <Label>
              <ogc:PropertyName>name</ogc:PropertyName>
            </Label>
            <Font>
              <CssParameter name="font-family">Times New Roman</CssParameter>
              <CssParameter name="font-style">Normal</CssParameter>
              <CssParameter name="font-size">14</CssParameter>
            </Font>
            <LabelPlacement>
              <PointPlacement>
                <AnchorPoint>
                  <AnchorPointX>0.5</AnchorPointX>
                  <AnchorPointY>0.5</AnchorPointY>
                </AnchorPoint>
              </PointPlacement>
            </LabelPlacement>
          </TextSymbolizer>
        </Rule>-->
     </FeatureTypeStyle>
    </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>