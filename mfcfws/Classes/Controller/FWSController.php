<?php
class Tx_Mkfws_Controller_FWSController extends Tx_Extbase_MVC_Controller_ActionController {

	public function initializeAction() {
	}
	
	public function indexAction() {
		$lines = file(PATH_site.'uploads/tx_mkfws/result.txt');
		$time = new DateTime($lines[0]);
		$values = array();
		for ($i = 2; $i <= 5; $i++) {
			if ($i == 3)
				continue;
			$arr = explode(':',$lines[$i]);
			$arr = t3lib_div::trimExplode(';',$arr[1]);
			$values[] = array('val' => floatval($arr[0]), 'dev' => floatval($arr[1]));
		}
		$weather = t3lib_div::makeInstance('Tx_Mkfws_Domain_Model_Weather',
			$time,$values[0]['val'],$values[0]['dev'],$values[1]['val'],$values[1]['dev'],$values[2]['val'],$values[2]['dev']);
		
		$this->view->assign('weather', $weather);
	}
}
?>