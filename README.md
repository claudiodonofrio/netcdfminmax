# netcdfminmax

Read a netcdf file and extract minimum and maximum
values for all variables.
A defined _FillValue for a variable is skipped in the output.
At the time of writing, a "missing_value" is not masked
and will be considered for min/max value.

Example result in JSON format:

{
	"lon": {
		"min": -11.958333015441895,
		"max": 29.95833396911621
	},
	"lat": {
		"min": 30.04166603088379,
		"max": 71.95833587646484
	},
	"time": {
		"min": 0.0,
		"max": 2526.0
	},
	"rn_flux": {
		"min": 0.0,
		"max": 170.732421875
	}
}


