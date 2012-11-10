package net.sf.sketchlet.common.ui.fisheye;

/**
 * Copyright (C) 1998-2000 by University of Maryland, College Park, MD 20742, USA
 * All rights reserved.
 *
 * @author Ben Bederson
 */
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FishEyeMenuTest extends JFrame implements ActionListener {

    int FOCUS_LENGTH = 11;
    int MAX_SIZE = 12;
    FishEyeMenu fishEyeMenu;
    JTextArea text;

    static public void main(String args[]) {
        new FishEyeMenuTest();
    }

    public FishEyeMenuTest() {
        setJMenuBar(createMenuBar(webSites));
        getContentPane().add(createContent());
        pack();
        setVisible(true);
    }

    JMenuBar createMenuBar(String[] entries) {
        JMenuBar menuBar = new JMenuBar();

        int i, j;
        JMenuItem item;

        ArrowMenu arrowMenu = new ArrowMenu("ArrowBar");
        for (i = 0; i < entries.length; i++) {
            item = new JMenuItem(entries[i]);
            item.addActionListener(this);
            arrowMenu.add(item);
        }
        menuBar.add(arrowMenu);

        ScrollMenu scrollMenu = new ScrollMenu("ScrollBar");
        for (i = 0; i < entries.length; i++) {
            item = new JMenuItem(entries[i]);
            item.addActionListener(this);
            scrollMenu.add(item);
        }
        menuBar.add(scrollMenu);

        JMenu menu = new JMenu("Hierarchy");
        JMenu subMenu = null;
        char label = ' ';

        for (i = 0; i < entries.length; i++) {
            if (label != Character.toUpperCase(entries[i].charAt(0))) {
                label = Character.toUpperCase(entries[i].charAt(0));
                subMenu = new JMenu(entries[i].substring(0, 1).toUpperCase());
                menu.add(subMenu);
            }
            item = new JMenuItem(entries[i]);
            item.addActionListener(this);
            subMenu.add(item);
        }
        menuBar.add(menu);

        fishEyeMenu = new FishEyeMenu("Fisheye");
        for (i = 0; i < entries.length; i += 1) {
            item = new JMenuItem(entries[i]);
            item.addActionListener(this);
            fishEyeMenu.add(item);
        }
        menuBar.add(fishEyeMenu);

        return menuBar;
    }

    Component createContent() {
        Box box = Box.createVerticalBox();
        getContentPane().add(box);

        box.add(Box.createVerticalStrut(50));

        // Content buttons
        Box contentBox = Box.createHorizontalBox();
        JLabel contentLabel = new JLabel("Menu Content: ");
        contentBox.add(contentLabel);

        ButtonGroup group = new ButtonGroup();
        JToggleButton websiteButton = new JToggleButton("100 Web Sites");
        websiteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JMenuBar menuBar = createMenuBar(webSites);
                setJMenuBar(menuBar);
                validate();
            }
        });
        websiteButton.setSelected(true);
        group.add(websiteButton);
        contentBox.add(websiteButton);

        JToggleButton countryButton = new JToggleButton("266 Countries");
        countryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JMenuBar menuBar = createMenuBar(countries);
                setJMenuBar(menuBar);
                validate();
            }
        });
        group.add(countryButton);
        contentBox.add(countryButton);
        contentBox.add(Box.createHorizontalGlue());
        box.add(contentBox);

        // Focus length scrollbar
        box.add(Box.createVerticalStrut(20));
        Box focusLengthBox = Box.createHorizontalBox();
        final JLabel focusLengthLabel = new JLabel("Focus Length: " + FOCUS_LENGTH + "  ");
        focusLengthBox.add(focusLengthLabel);
        final JScrollBar focusLengthBar = new JScrollBar(JScrollBar.HORIZONTAL, FOCUS_LENGTH, 1, 1, 21);
        focusLengthBar.addAdjustmentListener(new AdjustmentListener() {

            public void adjustmentValueChanged(AdjustmentEvent e) {
                fishEyeMenu.setDesiredFocusLength(focusLengthBar.getValue());
                focusLengthLabel.setText("Focus Length: " + focusLengthBar.getValue() + "  ");
            }
        });
        focusLengthBox.add(focusLengthBar);
        box.add(focusLengthBox);

        // Selection pane
        box.add(Box.createVerticalStrut(20));
        JLabel textLabel = new JLabel("Menu Selections:");
        Box menuSelectionBox = Box.createHorizontalBox();
        menuSelectionBox.add(textLabel);
        menuSelectionBox.add(Box.createHorizontalGlue());
        box.add(menuSelectionBox);
        text = new JTextArea();
        text.setBackground(Color.lightGray);
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.getViewport().setView(text);
        box.add(scrollPane);

        return box;
    }

    public void actionPerformed(ActionEvent e) {
        text.append(((JMenuItem) e.getSource()).getText() + "\n");
    }
    String[] webSites = {
        "About Portal",
        "AltaVista Search",
        "Amazon Shopping",
        "Andale Shopping",
        "Any Day Calendar",
        "AOL Instant Messager",
        "Ask Jeeves",
        "Auctions",
        "BigStep Small Business",
        "BizRate Surveys",
        "BizTravel",
        "Brittanica",
        "C|Net Technology",
        "Carnegie Mellon University",
        "CBS Sportsline",
        "CenterBeam Business",
        "Charles Schwab",
        "Chumbo Computer Shopping",
        "DealTime Shopping",
        "Deep Canyon Research",
        "Deja Opinions",
        "Dell Computer",
        "DevEdge Web Development",
        "DirectHit Search",
        "Discovery Channel for Kids",
        "Disney",
        "eBay Auctions",
        "eFax",
        "eHobbies",
        "eHow Do I ...?",
        "Epinions",
        "eVite Organizing",
        "eWanted Shopping",
        "Expedia Travel",
        "Fashion Mall",
        "FreeAgent",
        "Free Merchant Business",
        "Free Shop",
        "Furniture",
        "Garden",
        "Gateway 2000",
        "Georgia Tech",
        "Google Search",
        "Guru Net",
        "HiFi",
        "HotBot Search",
        "HotJobs",
        "Hot Office",
        "ICQ Online Communication",
        "Info Space",
        "Internet Movie Database",
        "iQVC Shopping",
        "Land's End",
        "Lonely Planet",
        "Lycos",
        "Massachusetts Institute of Technology",
        "McAfee Anti-Virus",
        "Mercata Shopping",
        "MindSpring ISP",
        "Monster Job Search",
        "My Simon Shopping",
        "MP3",
        "MSN",
        "My Help Desk",
        "NECX Computer Shopping",
        "New York University",
        "Northern Light Search",
        "Outpost Shopping",
        "Palace Visual Chat",
        "PC Magazine PC Labs",
        "PowWow Instant Messaging",
        "Productopia Shopping",
        "Quicken",
        "RealNetworks Streaming Media",
        "Remarq Collaboration Services",
        "Royal Institute of Technology",
        "Scour Media Search",
        "See U There Organizing",
        "Service 911 Computer Help",
        "SlashDot",
        "SmartAge Business",
        "Stamps",
        "Super Family Exchange",
        "Surf Monkey Kids Channel",
        "Swedish Institute of Computer Science",
        "Talk City Online Communities",
        "TD Waterhouse",
        "The Street Financial",
        "Topica Mailing Lists",
        "University of Maryland",
        "University of New Mexico",
        "US Federal Government",
        "WebFlyer Travel",
        "Web Monkey Web Development",
        "Welcome to EXP.com",
        "WWW Consortium",
        "XpertSite Questions",
        "Yahoo!",
        "ZCentral Business",
        "ZD Net"
    };
    String[] countries = {
        "Afghanistan",
        "Albania",
        "Algeria",
        "American Samoa",
        "Andorra",
        "Angola",
        "Anguilla",
        "Antarctica",
        "Antigua and Barbuda",
        "Arctic Ocean",
        "Argentina",
        "Armenia",
        "Aruba",
        "Ashmore and Cartier Islands",
        "Atlantic Ocean",
        "Australia",
        "Austria",
        "Azerbaijan",
        "Bahamas, The",
        "Bahrain",
        "Baker Island",
        "Bangladesh",
        "Barbados",
        "Bassas da India",
        "Belarus",
        "Belgium",
        "Belize",
        "Benin",
        "Bermuda",
        "Bhutan",
        "Bolivia",
        "Bosnia and Herzegovina",
        "Botswana",
        "Bouvet Island",
        "Brazil",
        "British Indian Ocean Territory",
        "British Virgin Islands",
        "Brunei",
        "Bulgaria",
        "Burkina Faso",
        "Burma",
        "Burundi",
        "Cambodia",
        "Cameroon",
        "Canada",
        "Cape Verde",
        "Cayman Islands",
        "Central African Republic",
        "Chad",
        "Chile",
        "China",
        "Christmas Island",
        "Clipperton Island",
        "Cocos (Keeling) Islands",
        "Colombia",
        "Comoros",
        "Congo, Democratic Republic of the",
        "Congo, Republic of the",
        "Cook Islands",
        "Coral Sea Islands",
        "Costa Rica",
        "Cote d'Ivoire",
        "Croatia",
        "Cuba",
        "Cyprus",
        "Czech Republic",
        "Denmark",
        "Djibouti",
        "Dominica",
        "Dominican Republic",
        "Ecuador",
        "Egypt",
        "El Salvador",
        "Equatorial Guinea",
        "Eritrea",
        "Estonia",
        "Ethiopia",
        "Europa Island",
        "Falkland Islands (Islas Malvinas)",
        "Faroe Islands",
        "Fiji",
        "Finland",
        "France",
        "French Guiana",
        "French Polynesia",
        "French Southern and Antarctic Lands",
        "Gabon",
        "Gambia, The",
        "Gaza Strip",
        "Georgia",
        "Germany",
        "Ghana",
        "Gibraltar",
        "Glorioso Islands",
        "Greece",
        "Greenland",
        "Grenada",
        "Guadeloupe",
        "Guam",
        "Guatemala",
        "Guernsey",
        "Guinea",
        "Guinea-Bissau",
        "Guyana",
        "Haiti",
        "Heard Island and McDonald Islands",
        "Honduras",
        "Hong Kong",
        "Howland Island",
        "Hungary",
        "Iceland",
        "India",
        "Indian Ocean",
        "Indonesia",
        "Iran",
        "Iraq",
        "Ireland",
        "Israel",
        "Italy",
        "Jamaica",
        "Jan Mayen",
        "Japan",
        "Jarvis Island",
        "Jersey",
        "Johnston Atoll",
        "Jordan",
        "Juan de Nova Island",
        "Kazakhstan",
        "Kenya",
        "Kingman Reef",
        "Kiribati",
        "Korea, North",
        "Korea, South",
        "Kuwait",
        "Kyrgyzstan",
        "Laos",
        "Latvia",
        "Lebanon",
        "Lesotho",
        "Liberia",
        "Libya",
        "Liechtenstein",
        "Lithuania",
        "Luxembourg",
        "Macau",
        "Macedonia, The Former Yugoslav Republic of",
        "Madagascar",
        "Malawi",
        "Malaysia",
        "Maldives",
        "Mali",
        "Malta",
        "Man, Isle of",
        "Marshall Islands",
        "Martinique",
        "Mauritania",
        "Mauritius",
        "Mayotte",
        "Mexico",
        "Micronesia, Federated States of",
        "Midway Islands",
        "Moldova",
        "Monaco",
        "Mongolia",
        "Montserrat",
        "Morocco",
        "Mozambique",
        "Namibia",
        "Nauru",
        "Navassa Island",
        "Nepal",
        "Netherlands",
        "Netherlands Antilles",
        "New Caledonia",
        "New Zealand",
        "Nicaragua",
        "Niger",
        "Nigeria",
        "Niue",
        "Norfolk Island",
        "Northern Mariana Islands",
        "Norway",
        "Oman",
        "Pacific Ocean",
        "Pakistan",
        "Palau",
        "Palmyra Atoll",
        "Panama",
        "Papua New Guinea",
        "Paracel Islands",
        "Paraguay",
        "Peru",
        "Philippines",
        "Pitcairn Islands",
        "Poland",
        "Portugal",
        "Puerto Rico",
        "Qatar",
        "Reunion",
        "Romania",
        "Russia",
        "Rwanda",
        "Saint Helena",
        "Saint Kitts and Nevis",
        "Saint Lucia",
        "Saint Pierre and Miquelon",
        "Saint Vincent and the Grenadines",
        "Samoa",
        "San Marino",
        "Sao Tome and Principe",
        "Saudi Arabia",
        "Senegal",
        "Serbia and Montenegro",
        "Seychelles",
        "Sierra Leone",
        "Singapore",
        "Slovakia",
        "Slovenia",
        "Solomon Islands",
        "Somalia",
        "South Africa",
        "South Georgia and the South Sandwich Islands",
        "Spain",
        "Spratly Islands",
        "Sri Lanka",
        "Sudan",
        "Suriname",
        "Svalbard",
        "Swaziland",
        "Sweden",
        "Switzerland",
        "Syria",
        "Taiwan",
        "Tajikistan",
        "Tanzania",
        "Thailand",
        "Togo",
        "Tokelau",
        "Tonga",
        "Trinidad and Tobago",
        "Tromelin Island",
        "Tunisia",
        "Turkey",
        "Turkmenistan",
        "Turks and Caicos Islands",
        "Tuvalu",
        "Uganda",
        "Ukraine",
        "United Arab Emirates",
        "United Kingdom",
        "United States",
        "Uruguay",
        "Uzbekistan",
        "Vanuatu",
        "Venezuela",
        "Vietnam",
        "Virgin Islands",
        "Wake Atoll",
        "Wallis and Futuna",
        "West Bank",
        "Western Sahara",
        "World",
        "Yemen",
        "Zaire",
        "Zambia",
        "Zimbabwe"
    };
}
