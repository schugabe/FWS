<?php
if(!defined('TYPO3_MODE'))
	die('Access denied.');

t3lib_extMgm::addStaticFile($_EXTKEY, 'Configuration/TypoScript','Fligh Weather Station');

Tx_Extbase_Utility_Extension::registerPlugin($_EXTKEY,'pi1','Weather Display');
Tx_Extbase_Utility_Extension::configurePlugin($_EXTKEY,'pi1',array('FWS' => 'index'),array('FWS' => 'index'));

?>