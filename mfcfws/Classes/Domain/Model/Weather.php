<?php
/**
* Model for weather.
* @entity
*/
class Tx_Mkfws_Domain_Model_Weather extends Tx_Extbase_DomainObject_AbstractEntity {
	/**
	* @var DateTime
	*/
	protected $time;
	/**
	* @var Double
	*/
	protected $temperature;
	/**
	* @var Double
	*/
	protected $temperatureDeviation;
	/**
	* @var Double
	*/
	protected $winddirection;
	/**
	* @var Double
	*/
	protected $winddirectionDeviation;
	/**
	* @var Double
	*/
	protected $windspeed;
	/**
	* @var Double
	*/
	protected $windspeedDeviation;
	
	public function __construct($time,$speed,$speedD,$temp,$tempD,$dir,$dirD) {
		$this->time = $time;
		$this->temperature = $temp;
		$this->temperatureDeviation = $tempD;
		$this->winddirection = $dir;
		$this->winddirectionDeviation = $dirD;
		$this->windspeed = $speed;
		$this->windspeedDeviation = $speedD;
	}
	
	/**
	* Gets the time of data beeing recorded.
	* @return DateTime Time of data
	*/
	public function getTime() {
			return $this->time;
	}
	
	/**
	* Gets the current temperature.
	* @return Double Temperature
	*/
	public function getTemperature() {
			return $this->temperature;
	}
	
	/**
	* Gets the current wind direction.
	* @return Double Wind direction
	*/
	public function getWinddirection() {
			return $this->winddirection;
	}
	
	/**
	* Gets the current wind speed.
	* @return Double Wind speed
	*/
	public function getWindspeed() {
			return $this->windspeed;
	}
	
	/**
	* Gets the standard deviation of temperature.
	* @return Double Temperature deviation
	*/
	public function getTemperatureDeviation() {
			return $this->temperatureDeviation;
	}
	
	/**
	* Gets the standard deviation of current wind direction.
	* @return Double Wind direction deviation
	*/
	public function getWinddirectionDeviation() {
			return $this->winddirectionDeviation;
	}
	
	/**
	* Gets the standard deviation of current wind speed.
	* @return Double Wind speed deviation
	*/
	public function getWindspeedDeviation() {
			return $this->windspeedDeviation;
	}
}
?>