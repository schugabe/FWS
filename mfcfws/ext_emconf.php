<?php

########################################################################
# Extension Manager/Repository config file for ext "mkfws".
#
# Auto generated 15-06-2010 21:07
#
# Manual updates:
# Only the data in the array - everything else is removed by next
# writing. "version" and "dependencies" must not be touched!
########################################################################

$EM_CONF[$_EXTKEY] = array(
	'title' => 'Flight Weather Station',
	'description' => '',
	'category' => 'plugin',
	'author' => 'Markus Klein',
	'author_email' => 'm.klein@mfc-linz.at',
	'author_company' => '',
	'shy' => '',
	'dependencies' => 'extbase,fluid',
	'conflicts' => '',
	'priority' => '',
	'module' => '',
	'state' => 'stable',
	'internal' => '',
	'uploadfolder' => 1,
	'createDirs' => '',
	'modify_tables' => '',
	'clearCacheOnLoad' => 1,
	'lockType' => '',
	'version' => '0.1.0',
	'constraints' => array(
		'depends' => array(
			'php' => '5.2.0-0.0.0',
			'typo3' => '4.3.0-4.4.99',
			'extbase' => '1.0.1-0.0.0',
			'fluid' => '1.0.1-0.0.0',
		),
		'conflicts' => array(
		),
		'suggests' => array(
		),
	),
	'_md5_values_when_last_written' => 'a:11:{s:12:"ext_icon.gif";s:4:"1bdc";s:17:"ext_localconf.php";s:4:"5546";s:14:"ext_tables.php";s:4:"c685";s:36:"Classes/Controller/FWSController.php";s:4:"6f22";s:32:"Classes/Domain/Model/Weather.php";s:4:"499e";s:38:"Configuration/TypoScript/constants.txt";s:4:"8105";s:34:"Configuration/TypoScript/setup.txt";s:4:"439b";s:42:"Resources/Private/Templates/FWS/index.html";s:4:"0544";s:39:"Resources/Public/Stylesheets/styles.css";s:4:"1a38";s:19:"doc/wizard_form.dat";s:4:"1b7d";s:20:"doc/wizard_form.html";s:4:"94da";}',
	'suggests' => array(
	),
);

?>