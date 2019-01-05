
// This program gives the ability to easily add a point theme from
// a file of points with lines in the form longitude,latitude,name
// in world coordinates.  We also give the user a choice of creating
// a new shape file.  Letting the user browse to a folder would prevent
// a LOT of user errors.  This should really be done......maybe later.

import javax.swing.*;
import java.io.*;
import java.util.Vector;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.awt.*;
import java.awt.event.*;
import com.esri.mo2.ui.bean.*; // beans used: Map,Layer,Toc,TocAdapter,Tool
        // TocEvent,Legend(a legend is part of a toc),ActateLayer
import com.esri.mo2.ui.tb.ZoomPanToolBar;
import com.esri.mo2.ui.tb.SelectionToolBar;
import com.esri.mo2.ui.ren.LayerProperties;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import com.esri.mo2.data.feat.*; //ShapefileFolder, ShapefileWriter
import com.esri.mo2.map.dpy.FeatureLayer;
import com.esri.mo2.map.dpy.BaseFeatureLayer;
import com.esri.mo2.map.draw.SimpleMarkerSymbol;
import com.esri.mo2.map.draw.BaseSimpleRenderer;
import com.esri.mo2.map.draw.TrueTypeMarkerSymbol;
import com.esri.mo2.file.shp.*;
import com.esri.mo2.map.dpy.Layerset;
import com.esri.mo2.ui.bean.Tool;
import java.awt.geom.*;
import com.esri.mo2.cs.geom.*;
import java.awt.Cursor; //using Envelope, Point, BasePointsArray
import com.esri.mo2.ui.dlg.AboutBox;
import java.util.PropertyResourceBundle; //change 6
import java.util.ResourceBundle;
import java.util.Locale;
import java.net.*;
import com.esri.mo2.map.draw.BaseSimpleLabelRenderer;
import com.esri.mo2.map.draw.*;

public class QuickStartXY3 extends JFrame {
 ResourceBundle names;
  Locale loc1 = new Locale("es","MX"); //change 7
  Locale loc2 = new Locale("en","US");
  
  static Map map = new Map();
  static boolean fullMap = true;  // Map not zoomed
  static boolean helpToolOn;
  Legend legend;
  Legend legend2;
  Layer layer = new Layer();
  Layer layer2 = new Layer();
  Layer mylayer = new Layer();
  
  Layer layer3 = null;
  static AcetateLayer acetLayer;
  static com.esri.mo2.map.dpy.Layer layer4;
  com.esri.mo2.map.dpy.Layer activeLayer;
  int activeLayerIndex;
  com.esri.mo2.cs.geom.Point initPoint,endPoint;
  double distance;
  JMenuBar mbar = new JMenuBar();
  JMenu file = new JMenu("File");
  JMenu theme = new JMenu("Theme");
  JMenu layercontrol = new JMenu("Layer Control");
   JMenu help = new JMenu("Help");
  JMenuItem attribitem = new JMenuItem("open attribute table",
                            new ImageIcon("tableview.gif"));
  JMenuItem createlayeritem  = new JMenuItem("create layer from selection",
                    new ImageIcon("Icon0915b.jpg"));
  static JMenuItem promoteitem = new JMenuItem("promote selected layer",
                    new ImageIcon("promote.jpg"));
  JMenuItem demoteitem = new JMenuItem("demote selected layer",
                    new ImageIcon("demote.jpg"));
  JMenuItem printitem = new JMenuItem("print",new ImageIcon("print.gif"));
  JMenuItem addlyritem = new JMenuItem("add layer",new ImageIcon("addtheme.gif"));
  JMenuItem remlyritem = new JMenuItem("remove layer",new ImageIcon("delete.gif"));
  JMenuItem propsitem = new JMenuItem("Legend Editor",new ImageIcon("properties.gif"));

	JMenu helptopics = new JMenu("Help Topics");
  JMenuItem tocitem = new JMenuItem("Table of Contents",new ImageIcon("helptopic.gif"));
  JMenuItem legenditem = new JMenuItem("Legend Editor",new ImageIcon("helptopic.gif"));
  JMenuItem layercontrolitem = new JMenuItem("Layer Control",new ImageIcon("helptopic.gif"));
  JMenuItem helptoolitem = new JMenuItem("Help Tool",new ImageIcon("help2.gif"));
  JMenuItem contactitem = new JMenuItem("Contact us");
  JMenuItem aboutitem = new JMenuItem("About MOJO...");
    


   JButton englishjb = new JButton("English");
     JButton spanishjb = new JButton("Spanish");
	   ActionListener lang;
	   
 Toc toc = new Toc();

  String s1 = "C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\SanDiego.shp"; 
String s2 = "C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\ShoppingMalls.shp"; 
	String datapathname = "";
  String legendname = "";
  ZoomPanToolBar zptb = new ZoomPanToolBar();
  static SelectionToolBar stb = new SelectionToolBar();
  JToolBar jtb = new JToolBar();
  ComponentListener complistener;
  static String mystate = null;
  JLabel statusLabel = new JLabel("San Diego County  status bar    LOC");
  static JLabel milesLabel = new JLabel("   DIST:  0 mi    ");
  static JLabel kmLabel = new JLabel("  0 km    ");
  java.text.DecimalFormat df = new java.text.DecimalFormat("0.000");
  JPanel myjp = new JPanel();
  JPanel myjp2 = new JPanel();
  JButton prtjb = new JButton(new ImageIcon("print.gif"));
  JButton addlyrjb = new JButton(new ImageIcon("addtheme.gif"));
  JButton ptrjb = new JButton(new ImageIcon("pointer.gif"));
  JButton distjb = new JButton(new ImageIcon("measure_1.gif"));
  JButton XYjb = new JButton("XY");
  
  JButton hotjb = new JButton(new ImageIcon("hotlink.gif"));
  Toolkit tk = Toolkit.getDefaultToolkit();
  Image bolt = tk.getImage("hotlink.gif");  // 16x16 gif file
  java.awt.Cursor boltCursor = tk.createCustomCursor(bolt,new java.awt.Point(6,30),"bolt");
  
  //Arrow arrow = new Arrow();
  //DistanceTool distanceTool= new DistanceTool();
    JButton helpjb = new JButton(new ImageIcon("help2.gif"));

  static HelpTool helpTool = new HelpTool();
  ActionListener lis;
  ActionListener layerlis;
  ActionListener layercontrollis;
  TocAdapter mytocadapter;
    ActionListener helplis;
   MyPickAdapter picklis = new MyPickAdapter();
  Identify hotlink = new Identify(); //the Identify class implements a PickListener,
  class MyPickAdapter implements PickListener {

		//implements hotlink
    public void beginPick(PickEvent pe){
	System.out.println("begin pick");
   
	}  // this fires even when you click outside the states layer
 
    public void foundData(PickEvent pe){
			System.out.println("inside found data");	
	FeatureLayer flayer2 = (FeatureLayer) pe.getLayer();
      com.esri.mo2.data.feat.Cursor c = pe.getCursor();
      Feature f = null;
      Fields fields = null;
      if (c != null)
        f = (Feature)c.next();
      fields = f.getFields();
      String sname = fields.getField(4).getName(); //gets col. name for state name
      mystate = (String)f.getValue(4);
	  System.out.println(mystate);
	  System.out.println(sname);
	  System.out.println(flayer2);
	  System.out.println(f);
	  try {
		//HotPick hotpick = new HotPick(fivalue, row,activeLayerIndex,map);//opens dialog window with Duke in it
		HotPick hotpick = new HotPick(f);//opens dialog window with Duke in it
		
		hotpick.setVisible(true);
	  } catch(Exception e){}
    }
 
  
     public void endPick(PickEvent pe)
	{
	}
 
 };
  static Envelope env;
  public QuickStartXY3() {

    super("San Diego County");
	helpToolOn = false;
    //distanceTool.setMeasureUnit(com.esri.mo2.util.Units.MILES);
    //map.setMapUnit(com.esri.mo2.util.Units.MILES);
    this.setBounds(100,100,700,450);
    zptb.setMap(map);
    stb.setMap(map);
    setJMenuBar(mbar);

	
    ActionListener lisZoom = new ActionListener() {
	  public void actionPerformed(ActionEvent ae){
	    fullMap = false;}}; // can change a boolean here
	ActionListener lisFullExt = new ActionListener() {
	  public void actionPerformed(ActionEvent ae){
	    fullMap = true;}};
		
	MouseAdapter mlLisZoom = new MouseAdapter() {
	  public void mousePressed(MouseEvent me) {
		if (SwingUtilities.isRightMouseButton(me) && helpToolOn) {
	      try {
	        HelpDialog helpdialog = new HelpDialog((String)helpText.get(4));
            helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
      }
    };
    MouseAdapter mlLisZoomActive = new MouseAdapter() {
	  public void mousePressed(MouseEvent me) {
		if (SwingUtilities.isRightMouseButton(me) && helpToolOn) {
	      try {
		  	HelpDialog helpdialog = new HelpDialog((String)helpText.get(5));
		    helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	  }
    };
	 MouseAdapter mlZoutLisZoom = new MouseAdapter() {
	  public void mousePressed(MouseEvent me) {
		if (SwingUtilities.isRightMouseButton(me) && helpToolOn) {
	      try {
		  	HelpDialog helpdialog = new HelpDialog((String)helpText.get(6));
		    helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	  }
    };
	
	MouseAdapter mlLisZoomFull = new MouseAdapter() {
	  public void mousePressed(MouseEvent me) {
		if (SwingUtilities.isRightMouseButton(me) && helpToolOn) {
	      try {
		  	HelpDialog helpdialog = new HelpDialog((String)helpText.get(7));
		    helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	  }
    };
	// next line gets ahold of a reference to the zoomin button
	JButton zoomInButton = (JButton)zptb.getActionComponent("ZoomIn");
	JButton zoomOutButton = (JButton)zptb.getActionComponent("ZoomOut");
	JButton zoomFullExtentButton = (JButton)zptb.getActionComponent("ZoomToFullExtent");
	JButton zoomToSelectedLayerButton = (JButton)zptb.getActionComponent("ZoomToSelectedLayer");
	zoomInButton.addActionListener(lisZoom);
	zoomFullExtentButton.addActionListener(lisFullExt);
	zoomToSelectedLayerButton.addActionListener(lisZoom);
	
		zoomInButton.addMouseListener(mlLisZoom);
		zoomOutButton.addMouseListener(mlZoutLisZoom);
		zoomToSelectedLayerButton.addMouseListener(mlLisZoomActive);
		zoomFullExtentButton.addMouseListener(mlLisZoomFull);
		
	complistener = new ComponentAdapter () {
	  public void componentResized(ComponentEvent ce) {
	    if(fullMap) {
	      map.setExtent(env);
	      map.zoom(1.0);    //scale is scale factor in pixels
	      map.redraw();
	    }
	  }
	};
    addComponentListener(complistener);
    lis = new ActionListener() {public void actionPerformed(ActionEvent ae){
	  Object source = ae.getSource();
	  if (source == prtjb || source instanceof JMenuItem ) {
        com.esri.mo2.ui.bean.Print mapPrint = new com.esri.mo2.ui.bean.Print();
        mapPrint.setMap(map);
        mapPrint.doPrint();// prints the map
        }
      else if (source == ptrjb) {
		Arrow arrow = new Arrow();
		map.setSelectedTool(arrow);
	    }
	  else if (source == distjb) {
		DistanceTool distanceTool = new DistanceTool();
		map.setSelectedTool(distanceTool);
        }
	  else if (source == XYjb) {
		try {
		  AddXYtheme addXYtheme = new AddXYtheme();
		  addXYtheme.setMap(map);
		  addXYtheme.setVisible(false);// the file chooser needs a parent
		    // but the parent can stay behind the scenes
		  map.redraw();
		  } catch (IOException e){}
	    }
	else if (source == hotjb) {
        hotlink.setCursor(boltCursor); //set cursor for the tool
        map.setSelectedTool(hotlink);
      }
	   else if (source == helpjb) {
		helpToolOn = true;
		map.setSelectedTool(helpTool);
	  }
	  else
	    {
		try {
	      AddLyrDialog aldlg = new AddLyrDialog();
	      aldlg.setMap(map);
	      aldlg.setVisible(true);
	    } catch(IOException e){}
      }
    }};
    layercontrollis = new ActionListener() {public void
                actionPerformed(ActionEvent ae){
	  String source = ae.getActionCommand();
	  System.out.println(activeLayerIndex+" active index");
	  if (source.equals("promote selected layer") ||source.equals("Promover la capa seleccionada"))
		map.getLayerset().moveLayer(activeLayerIndex,++activeLayerIndex);
      else
        map.getLayerset().moveLayer(activeLayerIndex,--activeLayerIndex);
      enableDisableButtons();
      map.redraw();
    }};
	
	    helplis = new ActionListener()
        {public void actionPerformed(ActionEvent ae){
	  Object source = ae.getSource();
	  if (source instanceof JMenuItem) {
		String arg = ae.getActionCommand();
		System.out.println(arg);
		if(arg.equals("About MOJO...") || arg.equals("Acerca de MOJO...")) {
          AboutBox aboutbox = new AboutBox();
          aboutbox.setProductName("MOJO");
          aboutbox.setProductVersion("2.0");
          aboutbox.setVisible(true);
          aboutbox.setLocation(100,100);
	    }
	    else if(arg.equals("Contact us") || arg.equals("Contáctenos")) {
		  try {
	        String s = "\n\n\n\n    Any enquiries should be addressed to " +
	        "\n\n\n       eckberg@edoras.sdsu.edu";
            HelpDialog helpdialog = new HelpDialog(s);
            helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	    else if(arg.equals("Table of Contents") || arg.equals("Tabla de contenido")) {
		  try {
	        HelpDialog helpdialog = new HelpDialog((String)helpText.get(0));
            helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	    else if(arg.equals("Legend Editor") || arg.equals("Editor de leyenda")) {
		  try {
	        HelpDialog helpdialog = new HelpDialog((String)helpText.get(1));
            helpdialog.setVisible(true);
          } catch(IOException e){}
	    }
	    else if(arg.equals("Layer Control") || arg.equals("Control de capa")) {
		  try {
	        HelpDialog helpdialog = new HelpDialog((String)helpText.get(2));
            helpdialog.setVisible(true);
          } catch(IOException e){}
		}
		else if(arg.equals("Help Tool") || arg.equals("Herramienta de ayuda"))  {
	      try {
            HelpDialog helpdialog = new HelpDialog((String)helpText.get(3));
            helpdialog.setVisible(true);
	      } catch(IOException e){}
	    }
	  }
    }};
	
	lang = new ActionListener() { //change 8
          public void actionPerformed(ActionEvent ae){
		
                  Object source = ae.getSource();
                  if (source == englishjb) {
				  System.out.println("inside english");
                        names = ResourceBundle.getBundle("NamesBundle",loc2);  //   
                    
					}

              if (source == spanishjb) {
			  
            names = ResourceBundle.getBundle("NamesBundle",loc1);
            System.out.println("inside spanish");
                  }


translate();
    }};

	
    layerlis = new ActionListener() {public void actionPerformed(ActionEvent ae){
	  Object source = ae.getSource();
	  if (source instanceof JMenuItem) {
		String arg = ae.getActionCommand();
		if(arg.equals("add layer") || arg.equals("Añadir capa")) {
          try {
	        AddLyrDialog aldlg = new AddLyrDialog();
	        aldlg.setMap(map);
	        aldlg.setVisible(true);
          } catch(IOException e){}
	      }
	    else if(arg.equals("remove layer") || arg.equals("Eliminar capa")) {
	      try {
			com.esri.mo2.map.dpy.Layer dpylayer =
			   legend.getLayer();
			map.getLayerset().removeLayer(dpylayer);
			map.redraw();
			remlyritem.setEnabled(false);
			propsitem.setEnabled(false);
			attribitem.setEnabled(false);
			promoteitem.setEnabled(false);
			demoteitem.setEnabled(false);
			stb.setSelectedLayer(null);
			zptb.setSelectedLayer(null);
	      } catch(Exception e) {}
	      }
	    else if(arg.equals("Legend Editor") || arg.equals("Editor de leyenda")) {
          LayerProperties lp = new LayerProperties();
          lp.setLegend(legend);
          lp.setSelectedTabIndex(0);
          lp.setVisible(true);
	    }
	    else if (arg.equals("open attribute table") || arg.equals("Abrir la tabla de atributos")) {
	      try {
	        layer4 = legend.getLayer();
            AttrTab attrtab = new AttrTab();
            attrtab.setVisible(true);
	      } catch(IOException ioe){}
	    }
        else if (arg.equals("create layer from selection") || arg.equals("Crear capa desde la selección")) {
	      com.esri.mo2.map.draw.BaseSimpleRenderer sbr = new
	        com.esri.mo2.map.draw.BaseSimpleRenderer();
		  com.esri.mo2.map.draw.SimpleFillSymbol sfs = new
		    com.esri.mo2.map.draw.SimpleFillSymbol();// for polygons
		  sfs.setSymbolColor(new Color(255,255,0)); // mellow yellow
		  sfs.setType(com.esri.mo2.map.draw.SimpleFillSymbol.FILLTYPE_SOLID);
		  sfs.setBoundary(true);
	      layer4 = legend.getLayer();
	      FeatureLayer flayer2 = (FeatureLayer)layer4;
	      // select, e.g., Montana and then click the
	      // create layer menuitem; next line verifies a selection was made
	      System.out.println("has selected" + flayer2.hasSelection());
	      //next line creates the 'set' of selections
	      if (flayer2.hasSelection()) {
		    SelectionSet selectset = flayer2.getSelectionSet();
	        // next line makes a new feature layer of the selections
	        FeatureLayer selectedlayer = flayer2.createSelectionLayer(selectset);
	        sbr.setLayer(selectedlayer);
	        sbr.setSymbol(sfs);
	        selectedlayer.setRenderer(sbr);
	        Layerset layerset = map.getLayerset();
	        // next line places a new visible layer, e.g. Montana, on the map
	        layerset.addLayer(selectedlayer);
	        //selectedlayer.setVisible(true);
	        if(stb.getSelectedLayers() != null)
	          promoteitem.setEnabled(true);
	        try {
	          legend2 = toc.findLegend(selectedlayer);
		    } catch (Exception e) {}

		    CreateShapeDialog csd = new CreateShapeDialog(selectedlayer);
		    csd.setVisible(true);
	        Flash flash = new Flash(legend2);
	        flash.start();
	        map.redraw(); // necessary to see color immediately

		  }
	    }
      }
    }};
    toc.setMap(map);
    mytocadapter = new TocAdapter() {
	  public void click(TocEvent e) {
		System.out.println(activeLayerIndex+ "dex");
	    legend = e.getLegend();
	    activeLayer = legend.getLayer();
	    stb.setSelectedLayer(activeLayer);
	    
	//	if(activeLayer.getName().equals("ShoppingMalls"))
		//{
		System.out.println("my layer :"+activeLayer.getName());
		com.esri.mo2.map.dpy.Layer[] mlayers = {activeLayer};
		hotlink.setSelectedLayers(mlayers);
		//}
		
		zptb.setSelectedLayer(activeLayer);
	    
		// get acive layer index for promote and demote
	    activeLayerIndex = map.getLayerset().indexOf(activeLayer);
	    // layer indices are in order added, not toc order.
	    System.out.println(activeLayerIndex + "active index");
		com.esri.mo2.map.dpy.Layer[] layers = {activeLayer};
		//System.out.println(activeLayer);
		
		/*if(activeLayer.getName() == "ShoppingMalls")
		{
			    System.out.println("inside my layer");

		com.esri.mo2.map.dpy.Layer hotlinkLayer=activeLayer;
		com.esri.mo2.map.dpy.Layer[] mlayers = {hotlinkLayer};
        hotlink.setSelectedLayers(mlayers);// replaces setToc from MOJ10
	    }*/
		remlyritem.setEnabled(true);
	    propsitem.setEnabled(true);
	    attribitem.setEnabled(true);
	    enableDisableButtons();
   	  }
    };
    map.addMouseMotionListener(new MouseMotionAdapter() {
	  public void mouseMoved(MouseEvent me) {
		com.esri.mo2.cs.geom.Point worldPoint = null;
		if (map.getLayerCount() > 0) {
		  worldPoint = map.transformPixelToWorld(me.getX(),me.getY());
		  String s = "X:"+df.format(worldPoint.getX())+" "+
		             "Y:"+df.format(worldPoint.getY());
		  statusLabel.setText("San Diego County   " + s);
	      }
	    else
	      statusLabel.setText(" San Diego County  X:0.000 Y:0.000");
      }
    });

	
 if (acetLayer != null) 
 QuickStartXY3.map.remove(acetLayer);

 acetLayer = new AcetateLayer() {
			public void paintComponent(java.awt.Graphics g) {
				java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
					g2d.setColor(new Color(250,0,0));
					g2d.drawString("San Diego County",700,150);	
		    }
	
		};
		acetLayer.setMap(QuickStartXY3.map);
        QuickStartXY3.map.add(acetLayer);
		
    toc.addTocListener(mytocadapter);
    remlyritem.setEnabled(false); // assume no layer initially selected
    propsitem.setEnabled(false);
    attribitem.setEnabled(false);
    promoteitem.setEnabled(false);
    demoteitem.setEnabled(false);
    printitem.addActionListener(lis);
    addlyritem.addActionListener(layerlis);
    remlyritem.addActionListener(layerlis);
    propsitem.addActionListener(layerlis);
    attribitem.addActionListener(layerlis);
    createlayeritem.addActionListener(layerlis);
    promoteitem.addActionListener(layercontrollis);
    demoteitem.addActionListener(layercontrollis);
	
	tocitem.addActionListener(helplis);
    legenditem.addActionListener(helplis);
    layercontrolitem.addActionListener(helplis);
    helptoolitem.addActionListener(helplis);
    contactitem.addActionListener(helplis);
    aboutitem.addActionListener(helplis);
	
    file.add(addlyritem);
    file.add(printitem);
    file.add(remlyritem);
    file.add(propsitem);
    theme.add(attribitem);
    theme.add(createlayeritem);
    layercontrol.add(promoteitem);
    layercontrol.add(demoteitem);
	
	 help.add(helptopics);
    helptopics.add(tocitem);
    helptopics.add(legenditem);
    helptopics.add(layercontrolitem);
    help.add(helptoolitem);
    help.add(contactitem);
    help.add(aboutitem);
	
    mbar.add(file);
    mbar.add(theme);
    mbar.add(layercontrol);
	
	    mbar.add(help);

    prtjb.addActionListener(lis);
    prtjb.setToolTipText("print map");
    addlyrjb.addActionListener(lis);
    addlyrjb.setToolTipText("add layer");
    ptrjb.addActionListener(lis);
    distjb.addActionListener(lis);
    XYjb.addActionListener(lis);
	helpjb.addActionListener(lis);
    XYjb.setToolTipText("add a layer of points from a file");
    prtjb.setToolTipText("print");
    distjb.setToolTipText("press-drag-release to measure a distance");
	
	hotlink.addPickListener(picklis);
    hotlink.setPickWidth(5);// sets tolerance for hotlink clicks
    hotjb.addActionListener(lis);
    hotjb.setToolTipText("hotlink tool--click somthing to maybe see a picture");
	 helpjb.setToolTipText("left click here, then right click on a tool to learn about that tool;"+
       "click arrow tool when done");
	jtb.add(hotjb);
	 
    jtb.add(prtjb);
    jtb.add(addlyrjb);
    jtb.add(ptrjb);
    jtb.add(distjb);
    jtb.add(XYjb);
	 jtb.add(helpjb);
	 
	 
    myjp.add(jtb);
    myjp.add(zptb); myjp.add(stb);
    myjp2.add(statusLabel);
    myjp2.add(milesLabel);myjp2.add(kmLabel);
	    setuphelpText();

    
		englishjb.addActionListener(lang);
        englishjb.setToolTipText("select english language");
        spanishjb.addActionListener(lang);
        spanishjb.setToolTipText("select spanish language");
	
	myjp.add(englishjb);
    myjp.add(spanishjb);
    getContentPane().add(map, BorderLayout.CENTER);
    getContentPane().add(myjp,BorderLayout.NORTH);
    getContentPane().add(myjp2,BorderLayout.SOUTH);

	addShapefileToMap(layer,s1);
	addShapefileToMap(mylayer,s2);
  
    getContentPane().add(toc, BorderLayout.WEST);
	
	java.util.List mylist = toc.getAllLegends();
	com.esri.mo2.map.dpy.Layer dpylayer1 = ((Legend)mylist.get(0)).getLayer();
     FeatureLayer flayer24 = (FeatureLayer)dpylayer1;
	 BaseSimpleLabelRenderer bslr1 = new BaseSimpleLabelRenderer();
	 FeatureClass fclass24 = flayer24.getFeatureClass();
	 String mycolnames[] = fclass24.getFields().getNames();
	 System.out.println(mycolnames[4]);
	 Fields myfields = fclass24.getFields();
	 Field myfield = myfields.getField(4);
	 bslr1.setLabelField(myfield);
	flayer24.setLabelRenderer(bslr1);
	
	
	flayer24.setFeatureClass(fclass24);
	BaseSimpleRenderer mysrd = new BaseSimpleRenderer();
	/*TrueTypeMarkerSymbol myttm = new TrueTypeMarkerSymbol();
	myttm.setFont(new Font("ESRI Transportation & Civic",Font.PLAIN,60));// aka esri_9
	myttm.setColor(new Color(255,0,0));
	myttm.setCharacter(new ImageIcon("print.gif")); //airplane
	mysrd.setSymbol(myttm);
	flayer24.setRenderer(mysrd);
	*/
	
RasterMarkerSymbol rmSymbol = new RasterMarkerSymbol();
rmSymbol.setAntialiasing(true);
rmSymbol.setTransparency(0.8);
rmSymbol.setImageString( "mall1.jpg" );
rmSymbol.setSizeX(30); 
rmSymbol.setSizeY(30); 

mysrd.setSymbol(rmSymbol);
flayer24.setRenderer(mysrd);	
	
	
	com.esri.mo2.map.dpy.Layer sdlayer = ((Legend)mylist.get(1)).getLayer();
	 FeatureLayer sdflayer = (FeatureLayer)sdlayer;
	 BaseSimpleRenderer sdbsr = (BaseSimpleRenderer)sdflayer.getRenderer();
	 com.esri.mo2.map.draw.Symbol mysym= sdbsr.getSymbol();
	 com.esri.mo2.map.draw.SimplePolygonSymbol sps = (com.esri.mo2.map.draw.SimplePolygonSymbol)mysym;
	//sps.setPaint(com.esri.mo2.map.draw.AoFillStyle.getPaint(com.esri.mo2.map.draw.AoFillStyle.SOLID_FILL, new java.awt.Color(153,153,255)));
	sps.setPaint(com.esri.mo2.map.draw.AoFillStyle.getPaint(com.esri.mo2.map.draw.AoFillStyle.SOLID_FILL, new java.awt.Color(204,204,255)));
	sdbsr.setSymbol(sps);
	 
  }
  

	
 
  public void addShapefileToMap(Layer layer,String s) {
    String datapath = s; //"C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\States.shp";
    layer.setDataset("0;"+datapath);
    map.add(layer);
  }
  
   private void setuphelpText() {
	String s0 =
	  "    The toc, or table of contents, is to the left of the map. \n" +
	  "    Each entry is called a 'legend' and represents a map 'layer' or \n" +
	  "    'theme'.  If you click on a legend, that layer is called the \n" +
	  "    active layer, or selected layer.  Its display (rendering) properties \n" +
	  "    can be controlled using the Legend Editor, and the legends can be \n" +
	  "    reordered using Layer Control.  Both Legend Editor and Layer Control \n" +
	  "    are separate Help Topics.  This line is e... x... t... e... n... t... e... d"  +
	  "    to test the scrollpane.";
	helpText.add(s0);
	String s1 = "  The Legend Editor is a menu item found under the File menu. \n" +
	  "    Given that a layer is selected by clicking on its legend in the table of \n" +
	  "    contents, clicking on Legend Editor will open a window giving you choices \n" +
	  "    about how to display that layer.  For example you can control the color \n" +
	  "    used to display the layer on the map, or whether to use multiple colors ";
	helpText.add(s1);
	String s2 = "  Layer Control is a Menu on the menu bar.  If you have selected a \n"+
	   " layer by clicking on a legend in the toc (table of contents) to the left of \n" +
	   " the map, then the promote and demote tools will become usable.  Clicking on \n" +
	   " promote will raise the selected legend one position higher in the toc, and \n" +
	   " clicking on demote will lower that legend one position in the toc.";
	helpText.add(s2);
	String s3 = "    This tool will allow you to learn about certain other tools. \n" +
	  "    You begin with a standard left mouse button click on the Help Tool itself. \n" +
	  "    RIGHT click on another tool and a window may give you information about the  \n" +
	  "    intended usage of the tool.  Click on the arrow tool to stop using the \n" +
	  "    help tool.";
	helpText.add(s3);
	String s4 = "If you click on the Zoom In tool, and then click on the map, you \n" +
	  " will see a part of the map in greater detail.  You can zoom in multiple times. \n" +
	  " You can also sketch a rectangular part of the map, and zoom to that.  You can \n" +
	  " undo a Zoom In with a Zoom Out or with a Zoom to Full Extent";
	helpText.add(s4);
	String s5 = "You must have a selected layer to use the Zoom to Active Layer tool.\n" +
	  "    If you then click on Zoom to Active Layer, you will be shown enough of \n" +
	  "    the full map to see all of the features in the layer you select.  If you \n" +
	  "    select a layer that shows where glaciers are, then you do not need to \n" +
	  "    see Hawaii, or any southern states, so you will see Alaska, and northern \n" +
	  "    mainland states.";
	helpText.add(s5);
	String s6 = "If you click on the Zoom Out tool, and then click on the map, you \n" +
	  " will see a part of the map in less detail.  You can zoom out multiple times. \n" +
	  " You can undo a Zoom out with a Zoom In or with a Zoom to Full Extent";
	helpText.add(s6);
	String s7 = "If you click on the Zoom to Full Extent tool, you \n" +
	  " will see the map in original size.  You can use this \n" +
	  " tool to get back to original size \n"+
	  " after applying Zoom In or Zoom Out tool \n";
	helpText.add(s7);
  }
 
  public static void main(String[] args) {
    QuickStartXY3 qstart = new QuickStartXY3();
    qstart.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            System.out.println("Thanks, Quick Start exits");
            System.exit(0);
        }
    });
    qstart.setVisible(true);
    env = map.getExtent();
  }
 
public void translate() {          
System.out.print("inside translate");    
              
        file.setText(names.getString("File"));
		addlyritem.setText(names.getString("addLayer"));
        remlyritem.setText(names.getString("RemoveLayer"));
        printitem.setText(names.getString("Print"));
        propsitem.setText(names.getString("LegendEditor"));
   
        theme.setText(names.getString("Theme"));
        attribitem.setText(names.getString("OpenAttributeTable"));
        createlayeritem.setText(names.getString("CreateLayerFromSelection"));

        layercontrol.setText(names.getString("LayerControl"));
        promoteitem.setText(names.getString("PromoteItem"));
        demoteitem.setText(names.getString("DemoteItem"));
	
		help.setText(names.getString("Help"));
		helptopics.setText(names.getString("HelpTopics"));
		tocitem.setText(names.getString("TableOfContents"));
		legenditem.setText(names.getString("LegendEditor"));
		layercontrolitem.setText(names.getString("LayerControl"));
		helptoolitem.setText(names.getString("HelpTool"));  
        contactitem.setText(names.getString("Contact"));
        aboutitem.setText(names.getString("AboutMojo"));
		
		   englishjb.setText(names.getString("English"));
      spanishjb.setText(names.getString("Spanish"));
  }

 
 private void enableDisableButtons() {
    int layerCount = map.getLayerset().getSize();
    if (layerCount < 2) {
      promoteitem.setEnabled(false);
      demoteitem.setEnabled(false);
      }
    else if (activeLayerIndex == 0) {
      demoteitem.setEnabled(false);
      promoteitem.setEnabled(true);
	  }
    else if (activeLayerIndex == layerCount - 1) {
      promoteitem.setEnabled(false);
      demoteitem.setEnabled(true);
	  }
	else {
	  promoteitem.setEnabled(true);
	  demoteitem.setEnabled(true);
    }
  }
private ArrayList helpText = new ArrayList(3);
  }
// following is an Add Layer dialog window
class AddLyrDialog extends JDialog {
  Map map;
  ActionListener lis;
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JPanel panel1 = new JPanel();
  com.esri.mo2.ui.bean.CustomDatasetEditor cus = new com.esri.mo2.ui.bean.
    CustomDatasetEditor();
  AddLyrDialog() throws IOException {
	setBounds(50,50,520,430);
	setTitle("Select a theme/layer");
	addWindowListener(new WindowAdapter() {
	  public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
	lis = new ActionListener() {
	  public void actionPerformed(ActionEvent ae) {
	    Object source = ae.getSource();
	    if (source == cancel)
	      setVisible(false);
	    else {
	      try {
			setVisible(false);
			map.getLayerset().addLayer(cus.getLayer());
			map.redraw();
			if (QuickStartXY3.stb.getSelectedLayers() != null)
			  QuickStartXY3.promoteitem.setEnabled(true);
		  } catch(IOException e){}
	    }
	  }
    };
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    getContentPane().add(cus,BorderLayout.CENTER);
    panel1.add(ok);
    panel1.add(cancel);
    getContentPane().add(panel1,BorderLayout.SOUTH);
  }
  public void setMap(com.esri.mo2.ui.bean.Map map1){
	map = map1;
  }
}

class AddXYtheme extends JDialog {
  Map map;
  Vector s2 = new Vector();
  JFileChooser jfc = new JFileChooser();
  BasePointsArray bpa = new BasePointsArray();
  FeatureLayer XYlayer;
  AddXYtheme() throws IOException {
	setBounds(50,50,520,430);
	jfc.showOpenDialog(this);
	try {
	  File file  = jfc.getSelectedFile();
	  FileReader fred = new FileReader(file);
	  BufferedReader in = new BufferedReader(fred);
	  String s , x1 , y1; // = in.readLine();
	  double x,y;
	  int n = 0;
	  while ((s = in.readLine()) != null) {
		StringTokenizer st = new StringTokenizer(s,",");
		x1=st.nextToken();
		y1=st.nextToken();
		
		x = Double.parseDouble(x1);
		y = Double.parseDouble(y1);
	
		bpa.insertPoint(n++,new com.esri.mo2.cs.geom.Point(x,y));
		s2.addElement(x1);
		s2.addElement(y1);
		
		s2.addElement(st.nextToken());
		s2.addElement(st.nextToken());
		s2.addElement(st.nextToken());
		s2.addElement(st.nextToken());
		s2.addElement(st.nextToken());
	  
	  }
	} catch (IOException e){}
	XYfeatureLayer xyfl = new XYfeatureLayer(bpa,map,s2);
	XYlayer = xyfl;
	xyfl.setVisible(true);
	map = QuickStartXY3.map;
	map.getLayerset().addLayer(xyfl);
	map.redraw();
	CreateXYShapeDialog xydialog = new CreateXYShapeDialog(XYlayer);
	xydialog.setVisible(true);
  }
  public void setMap(com.esri.mo2.ui.bean.Map map1){
  	map = map1;
  }
}

class XYfeatureLayer extends BaseFeatureLayer {
  BaseFields fields;
  private java.util.Vector featureVector;
  public XYfeatureLayer(BasePointsArray bpa,Map map,Vector s2) {
	createFeaturesAndFields(bpa,map,s2);
	BaseFeatureClass bfc = getFeatureClass("MyPoints",bpa);
	setFeatureClass(bfc);
	BaseSimpleRenderer srd = new BaseSimpleRenderer();
	/*SimpleMarkerSymbol sms= new SimpleMarkerSymbol();
	sms.setType(SimpleMarkerSymbol.CIRCLE_MARKER);
	sms.setSymbolColor(new Color(255,0,0));
	sms.setWidth(5);
	srd.setSymbol(sms);*/
	
	TrueTypeMarkerSymbol ttm = new TrueTypeMarkerSymbol();
	ttm.setFont(new Font("ESRI Transportation & Civic",Font.PLAIN,40));// aka esri_9
	ttm.setColor(new Color(255,0,0));
	ttm.setCharacter("224"); //airplane
	srd.setSymbol(ttm);
	setRenderer(srd);
	
	// without setting layer capabilities, the points will not
	// display (but the toc entry will still appear)
	XYLayerCapabilities lc = new XYLayerCapabilities();
	setCapabilities(lc);
  }
  private void createFeaturesAndFields(BasePointsArray bpa,Map map,Vector s2) {
	featureVector = new java.util.Vector();
	fields = new BaseFields();
	createDbfFields();
	for(int i=0;i<bpa.size();i++) {
	  BaseFeature feature = new BaseFeature();  //feature is a row
	  feature.setFields(fields);
	  com.esri.mo2.cs.geom.Point p = new
	    com.esri.mo2.cs.geom.Point(bpa.getPoint(i));
	  feature.setValue(0,p);
	  System.out.println(" my no : "+fields.getNumFields());
	  feature.setValue(1,new Integer(0));  // point data
	for (int k=0;k<fields.getNumFields()-2;k++)
           {
             System.out.println(" element at: "+i*(fields.getNumFields()-2)+k);
               feature.setValue(k+2, (String) s2.elementAt(i*(fields.getNumFields()-2)+k));
               
               
           }
	
		feature.setDataID(new BaseDataID("MyPoints",i));
	  featureVector.addElement(feature);
	}
  }
 
 private void createDbfFields() {
	fields.addField(new BaseField("#SHAPE#",Field.ESRI_SHAPE,0,0));
	fields.addField(new BaseField("ID",java.sql.Types.INTEGER,9,0));
	// VARCHAR field width is VERY important--must be large enough
	fields.addField(new BaseField("Latitude",java.sql.Types.VARCHAR,35,0));
	fields.addField(new BaseField("Longitude",java.sql.Types.VARCHAR,35,0));
	fields.addField(new BaseField("Name",java.sql.Types.VARCHAR,100,0));
	fields.addField(new BaseField("Address",java.sql.Types.VARCHAR,250,0));
	fields.addField(new BaseField("PhoneNo",java.sql.Types.VARCHAR,20,0));
	fields.addField(new BaseField("Hours",java.sql.Types.VARCHAR,100,0));
	fields.addField(new BaseField("Website",java.sql.Types.VARCHAR,100,0));
	;
  }
  public BaseFeatureClass getFeatureClass(String name,BasePointsArray bpa){
    com.esri.mo2.map.mem.MemoryFeatureClass featClass = null;
    try {
	  featClass = new com.esri.mo2.map.mem.MemoryFeatureClass(MapDataset.POINT,
	    fields);
    } catch (IllegalArgumentException iae) {}
    featClass.setName(name);
    for (int i=0;i<bpa.size();i++) {
	  featClass.addFeature((Feature) featureVector.elementAt(i));
    }
    return featClass;
  }
  private final class XYLayerCapabilities extends
       com.esri.mo2.map.dpy.LayerCapabilities {
    XYLayerCapabilities() {
	  for (int i=0;i<this.size(); i++) {
		setAvailable(this.getCapabilityName(i),true);
		setEnablingAllowed(this.getCapabilityName(i),true);
		getCapability(i).setEnabled(true);
	  }
    }
  }
}

class AttrTab extends JDialog {
  JPanel panel1 = new JPanel();
  com.esri.mo2.map.dpy.Layer layer = QuickStartXY3.layer4;
  JTable jtable = new JTable(new MyTableModel());
  JScrollPane scroll = new JScrollPane(jtable);

  public AttrTab() throws IOException {
  	setBounds(70,70,450,350);
  	setTitle("Attribute Table for San Diego County");
  	addWindowListener(new WindowAdapter() {
  	  public void windowClosing(WindowEvent e) {
  	    setVisible(false);
  	  }
    });
    scroll.setHorizontalScrollBarPolicy(
	   JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	// next line necessary for horiz scrollbar to work
	jtable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	TableColumn tc = null;
	int numCols = jtable.getColumnCount();
	//jtable.setPreferredScrollableViewportSize(
		//new java.awt.Dimension(440,340));
	for (int j=0;j<numCols;j++) {
	  tc = jtable.getColumnModel().getColumn(j);
	  tc.setMinWidth(50);
    }
    getContentPane().add(scroll,BorderLayout.CENTER);
  }
}

class MyTableModel extends AbstractTableModel {
 // the required methods to implement are getRowCount,
 // getColumnCount, getValueAt
  com.esri.mo2.map.dpy.Layer layer = QuickStartXY3.layer4;
  MyTableModel() {
	qfilter.setSubFields(fields);
	com.esri.mo2.data.feat.Cursor cursor = flayer.search(qfilter);
	while (cursor.hasMore()) {
		ArrayList inner = new ArrayList();
		Feature f = (com.esri.mo2.data.feat.Feature)cursor.next();
		inner.add(0,String.valueOf(row));
		for (int j=1;j<fields.getNumFields();j++) {
		  inner.add(f.getValue(j).toString());
		}
	    data.add(inner);
	    row++;
    }
  }
  FeatureLayer flayer = (FeatureLayer) layer;
  FeatureClass fclass = flayer.getFeatureClass();
  String columnNames [] = fclass.getFields().getNames();
  ArrayList data = new ArrayList();
  int row = 0;
  int col = 0;
  BaseQueryFilter qfilter = new BaseQueryFilter();
  Fields fields = fclass.getFields();
  public int getColumnCount() {
  //System.out.println("col count : "+fclass.getFields().getNumFields());
	return fclass.getFields().getNumFields();
	
  }
  public int getRowCount() {
	return data.size();
  }
  public String getColumnName(int colIndx) {
	return columnNames[colIndx];
  }
  public Object getValueAt(int row, int col) {
	  ArrayList temp = new ArrayList();
	  temp =(ArrayList) data.get(row);
      return temp.get(col);
  }
}

class CreateShapeDialog extends JDialog {
  String name = "";
  String path = "";
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JTextField nameField = new JTextField("enter layer name here, then hit ENTER",25);
  com.esri.mo2.map.dpy.FeatureLayer selectedlayer;
  ActionListener lis = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == nameField) {
	  name = nameField.getText().trim();
	  path = ((ShapefileFolder)(QuickStartXY3.layer4.getLayerSource())).getPath();
	  System.out.println(path+"    " + name);
    }
	else if (o == cancel)
      setVisible(false);
	else {
	  try {
		ShapefileWriter.writeFeatureLayer(selectedlayer,path,name,0);
	  } catch(Exception e) {System.out.println("write error");}
	  setVisible(false);
    }
  }};

  JPanel panel1 = new JPanel();
  JLabel centerlabel = new JLabel();
  //centerlabel;
  CreateShapeDialog (com.esri.mo2.map.dpy.FeatureLayer layer5) {
	selectedlayer = layer5;
    setBounds(40,350,450,150);
    setTitle("Create new shapefile?");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
    nameField.addActionListener(lis);
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    String s = "<HTML> To make a new shapefile from the new layer, enter<BR>" +
      "the new name you want for the layer and click OK.<BR>" +
      "You can then add it to the map in the usual way.<BR>"+
      "Click ENTER after replacing the text with your layer name";
    centerlabel.setHorizontalAlignment(JLabel.CENTER);
    centerlabel.setText(s);
    getContentPane().add(centerlabel,BorderLayout.CENTER);
    panel1.add(nameField);
    panel1.add(ok);
    panel1.add(cancel);
    getContentPane().add(panel1,BorderLayout.SOUTH);
  }
}

class CreateXYShapeDialog extends JDialog {
  String name = "";
  String path = "";
  JButton ok = new JButton("OK");
  JButton cancel = new JButton("Cancel");
  JTextField nameField = new JTextField("enter layer name here, then hit ENTER",35);
  JTextField pathField = new JTextField("enter full path name here, then hit ENTER",35);
  com.esri.mo2.map.dpy.FeatureLayer XYlayer;
  ActionListener lis = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == pathField) {
	  path = pathField.getText().trim();
	  System.out.println(path);
    }
    else if (o == nameField) {
	  name = nameField.getText().trim();//this works
	  //path = ((ShapefileFolder)(QuickStartXY3.layer4.getLayerSource())).getPath();
	  System.out.println(path+"    " + name);
    }
	else if (o == cancel)
      setVisible(false);
	else {  // ok button clicked
	  try {
		ShapefileWriter.writeFeatureLayer(XYlayer,path,name,0);
		setVisible(false);
		
		String layerName=path+"\\"+name+".shp";
		
	AddNewShapeFile asfdialog = new AddNewShapeFile(layerName,XYlayer);
	asfdialog.setModal(true);
	asfdialog.setVisible(true);
		// the following hard-coded line worked with data.csv
		//ShapefileWriter.writeFeatureLayer(XYlayer,"C:\\esri\\moj20\\shapefile","aeroportals",0);
	  } catch(Exception e) {System.out.println(e);}
	  setVisible(false);
    }
  }};

  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JLabel centerlabel = new JLabel();
  //centerlabel;
  CreateXYShapeDialog (com.esri.mo2.map.dpy.FeatureLayer layer5) {
	XYlayer = layer5;
    setBounds(40,250,600,300);
    setTitle("Create new shapefile?");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
    nameField.addActionListener(lis);
    pathField.addActionListener(lis);
    ok.addActionListener(lis);
    cancel.addActionListener(lis);
    String s = "<HTML> To make a new shapefile from the new layer, enter<BR>" +
      "the new name you want for the layer and hit ENTER.<BR>" +
      "then enter a path to the folder you want to use <BR>" +
      "and hit ENTER once again <BR>" + "As an example type C:\\mylayers<BR>" +
      "You can then add it to the map in the usual way.<BR>"+
      "Click ENTER after replacing the text with your layer name";
    centerlabel.setHorizontalAlignment(JLabel.CENTER);
    centerlabel.setText(s);
    //getContentPane().add(centerlabel,BorderLayout.CENTER);
    panel1.add(centerlabel);
    panel1.add(nameField);
    panel1.add(pathField);
    panel2.add(ok);
    panel2.add(cancel);
    getContentPane().add(panel2,BorderLayout.SOUTH);
    getContentPane().add(panel1,BorderLayout.CENTER);
  }
}

class HelpDialog extends JDialog {
  JTextArea helptextarea;
  public HelpDialog(String inputText) throws IOException {
	setBounds(70,70,460,250);
  	setTitle("Help");
  	addWindowListener(new WindowAdapter() {
  	  public void windowClosing(WindowEvent e) {
  	    setVisible(false);
  	  }
    });
  	helptextarea = new JTextArea(inputText,7,40);
  	JScrollPane scrollpane = new JScrollPane(helptextarea);
    helptextarea.setEditable(false);
    getContentPane().add(scrollpane,"Center");
  }
}
class HelpTool extends Tool {
}
class Arrow extends Tool {

  Arrow() { // undo measure tool residue
    QuickStartXY3.milesLabel.setText("DIST   0 mi   ");
    QuickStartXY3.kmLabel.setText("   0 km    ");
    //QuickStartXY3.map.remove(QuickStartXY3.acetLayer);
    //QuickStartXY3.acetLayer = null;
    QuickStartXY3.map.repaint();
  }
}

class Flash extends Thread {
  Legend legend;
  Flash(Legend legendin) {
	legend = legendin;
  }
  public void run() {
	for (int i=0;i<12;i++) {
	  try {
		Thread.sleep(500);
		legend.toggleSelected();
	  } catch (Exception e) {}
    }
  }
}

class DistanceTool extends DragTool  {
  int startx,starty,endx,endy,currx,curry;
  com.esri.mo2.cs.geom.Point initPoint, endPoint, currPoint;
  double distance;
  public void mousePressed(MouseEvent me) {
	startx = me.getX(); starty = me.getY();
	initPoint = QuickStartXY3.map.transformPixelToWorld(me.getX(),me.getY());
  }
  public void mouseReleased(MouseEvent me) {
	  // now we create an acetatelayer instance and draw a line on it
	endx = me.getX(); endy = me.getY();
	endPoint = QuickStartXY3.map.transformPixelToWorld(me.getX(),me.getY());
    distance = (69.44 / (2*Math.PI)) * 360 * Math.acos(
				 Math.sin(initPoint.y * 2 * Math.PI / 360)
			   * Math.sin(endPoint.y * 2 * Math.PI / 360)
			   + Math.cos(initPoint.y * 2 * Math.PI / 360)
			   * Math.cos(endPoint.y * 2 * Math.PI / 360)
			   * (Math.abs(initPoint.x - endPoint.x) < 180 ?
                    Math.cos((initPoint.x - endPoint.x)*2*Math.PI/360):
                    Math.cos((360 - Math.abs(initPoint.x - endPoint.x))*2*Math.PI/360)));
    System.out.println( distance  );
    QuickStartXY3.milesLabel.setText("DIST: " + new Float((float)distance).toString() + " mi  ");
    QuickStartXY3.kmLabel.setText(new Float((float)(distance*1.6093)).toString() + " km");
    if (QuickStartXY3.acetLayer != null)
      QuickStartXY3.map.remove(QuickStartXY3.acetLayer);
    QuickStartXY3.acetLayer = new AcetateLayer() {
      public void paintComponent(java.awt.Graphics g) {
		java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
		Line2D.Double line = new Line2D.Double(startx,starty,endx,endy);
		g2d.setColor(new Color(0,0,250));
		g2d.draw(line);
      }
    };
    Graphics g = super.getGraphics();
    QuickStartXY3.map.add(QuickStartXY3.acetLayer);
    QuickStartXY3.map.redraw();
  }
  public void cancel() {};
}
//My code ********************************************************

class AddNewShapeFile extends JDialog {
String datapath;
FeatureLayer XYlayer1;
JButton yes = new JButton("Yes");
JButton no = new JButton("No");

  ActionListener list = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == no)
	{
     setVisible(false);
	}
	else {  // yes button clicked
	try 
	{
	Layer layer23 = new Layer();
	
	System.out.println("clicked yes");
	//QuickStartXY3 myinst= new QuickStartXY3();
	//datapath = "C:\\ESRI\\MOJ20\\Samples\\Data\\USA\\uslakes.shp";
    layer23.setDataset("0;"+datapath);

    QuickStartXY3.map.add(layer23);
	QuickStartXY3.map.redraw();
	
  	RemoveFeatureLayer rfldialog = new RemoveFeatureLayer(XYlayer1);
	rfldialog.setModal(true);
	rfldialog.setVisible(true);
	  }
	  catch(Exception e) {System.out.println("add shape file error");}
	  setVisible(false);
	  }
	  }
	  };

JPanel panel = new JPanel();
JLabel label = new JLabel("<html>Click yes if you want to add newly created shape file<br> else click no.<br></html>");


public AddNewShapeFile(String layername,FeatureLayer XYlayer) {
XYlayer1=XYlayer;
datapath=layername;

setBounds(40,250,500,300);
setTitle("Add new shapefile");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
yes.addActionListener(list);
no.addActionListener(list);
	
	label.setHorizontalAlignment(JLabel.CENTER);
    panel.add(label);
	panel.add(yes);
	panel.add(no);
	getContentPane().add(panel,BorderLayout.CENTER);

	}

  }

class RemoveFeatureLayer extends JDialog {
 FeatureLayer XYlayer1;
JButton yes = new JButton("Yes");
JButton no = new JButton("No");

  ActionListener list = new ActionListener() {public void actionPerformed(ActionEvent ae) {
	Object o = ae.getSource();
	if (o == no)
	{
     setVisible(false);
	}
	else { 
	try 
	{
	QuickStartXY3.map.getLayerset().removeLayer(XYlayer1);
	System.out.println("clicked yes");
	QuickStartXY3.map.redraw();
	  }
	  catch(Exception e) {System.out.println("Remove shape file error");}
	  setVisible(false);
	  }
	  }
	  };

JPanel panel = new JPanel();
JLabel label = new JLabel("<html>Click yes if you want to remove feature layer<br> else click no.<br></html>");

public RemoveFeatureLayer(FeatureLayer XYlayer) {
XYlayer1=XYlayer;
setBounds(40,250,500,300);
setTitle("Remove Feature Layer");
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	    setVisible(false);
	  }
    });
yes.addActionListener(list);
no.addActionListener(list);
	
	label.setHorizontalAlignment(JLabel.CENTER);
    panel.add(label);
	panel.add(yes);
	panel.add(no);
	getContentPane().add(panel,BorderLayout.CENTER);

	}

  }

 class HotPick extends JDialog {
 
   JPanel buttonPanel = new JPanel();
    JPanel infoPanel = new JPanel();
    JPanel imagePanel = new JPanel();
   
	static String OsType = null;
	
  String mystate = QuickStartXY3.mystate;

  String mallPic = null;
 
  String webUrl;
  ImageIcon mallIcon;
  JLabel mallLabel;
	
  String[][] malls={
{"Fashion Valley","10AM–9PM","fashion.JPG","http://www.simon.com/mall/fashion-valley"},

{"Westfield UTC","9AM–10PM","westutc.JPG","https://www.westfield.com/utc"},

{"Del Mar Highlands Town Center","5AM–12AM","delmar.JPEG","https://www.delmarhighlandstowncenter.com/"},

{"Grossmont Center","10AM–9PM","grossmont.JPG","http://www.grossmontcenter.com/"},

{"Seaport Village","10AM–9PM","seaport.JPG","http://www.seaportvillage.com/"},

{"Otay Ranch Town Center","10AM–10PM","otay.JPG","https://www.otayranchtowncenter.com/en.html"},

{"Carmel Mountain Plaza","10AM–8PM","carmel.JPG","https://www.carmelmountainplaza.com/"},

{"Parkway Plaza","10AM–10PM","parkway.JPG","https://www.shoppingparkwayplaza.com/"},

{"Westfield Mission Valley","10AM–9PM","westmission.JPG","https://www.westfield.com/missionvalley"},

{"Clairemont Town Square","5AM–2AM","clairemont.JPG","http://clairemonttownsquare.com/"},

};
  
  HotPick(Feature f) throws IOException 
 {
   if (OsType == null) {
            OsType = System.getProperty("os.name");
        }

System.out.println("inside HotPick");

JButton websiteBtn = new JButton("WebSite");
 ActionListener weblis;
			setTitle((String) f.getValue(4));
			setBounds(200,200,700,600);
            setResizable(true);
            setVisible(true);
			
			JLabel address = new JLabel();
			address.setText("Address : "+ f.getValue(5));
			//address.setFont(new Font("Serif", Font.BOLD, 18));
			System.out.println("add: "+f.getValue(5));
			
			JLabel contact = new JLabel();
            contact.setText(" Contact no : "+ f.getValue(6));
			//contact.setFont(new Font("Serif", Font.BOLD, 18));
			System.out.println("con " + f.getValue(6));
			
			JLabel hrs = new JLabel();
            hrs.setText("Opening Hours : "+ f.getValue(7));
			//hrs.setFont(new Font("Serif", Font.BOLD, 18));
			System.out.println("hrs " + f.getValue(7));
			
			buttonPanel.add(websiteBtn);
			System.out.println("mystate " +mystate);
            
			this.getContentPane().add(infoPanel, BorderLayout.NORTH);
            this.getContentPane().add(imagePanel, BorderLayout.CENTER);		
			this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			
 for (int i = 0;i<10;i++)  
{
System.out.println("mall "+malls[i][0]);
	  if (malls[i][0].equals(mystate)) {
 
	    mallPic = malls[i][2];
		webUrl = malls[i][3];
	    System.out.println(webUrl);
		mallIcon = new ImageIcon(mallPic);
		mallLabel = new JLabel(mallIcon);
			
				System.out.println("inside if loop");
			
			}
				System.out.println("inside for loop");				
    }
  
			infoPanel.add(address);
            infoPanel.add(contact);
			infoPanel.add(hrs);
			imagePanel.add(mallLabel);
			
		 weblis = new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    Object source = ae.getSource();
                    try {
					System.out.println("inside str type 1" + source);
					System.out.println(OsType);
                        if (source == websiteBtn) {
                            try {
                                if (OsType.indexOf("Windows") != -1) {
								System.out.println("inside str type 2");
                                    Runtime.getRuntime().exec(
                                            "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE "
                                                    + webUrl);
                                } else if (OsType.indexOf("Mac") != -1) {
                                    Runtime.getRuntime().exec(
                                            new String[]{"open", "-a",
                                                    "Safari", webUrl});

                                }
                            } catch (Exception e) {
                                System.out.println("Can not execute command. "
                                        + e);
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            };

            
		websiteBtn.addActionListener(weblis);
  }
}

