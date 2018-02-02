function setupCharts (ctx) {
	var $ctx = !!ctx ? $(ctx.parentNode || document) : $(ctx || document)
	$ctx.find('[data-chart-type]').each(function (idx, target) {
		//noinspection JSUnresolvedVariable
		if (!target.chart) {
			setupChart(target)
		}
	})
}

function setupChart (target) {
	function addCanvas (target) {
		var canvases = target.getElementsByTagName('canvas')
		for (var i = 0; i < canvases.length; i++) {
			target.removeChild(canvases[i])
		}
		var canvas = document.createElement('canvas')
		target.appendChild(canvas)
		return canvas
	}

	function clearSvgs (target) {
		var svgs = target.getElementsByTagName('svg')
		for (var i = 0; i < svgs.length; i++) {
			target.removeChild(svgs[i])
		}
	}

	var CHART_TYPES = function chartTypesInit () {
		var gauge = {
			name: 'gauge',
			isLegendSupported: false,
			createChart: function setupGauge (target, data, options) {
				clearSvgs(target)
				options.id = target.getAttribute('id')
				//noinspection JSUnresolvedVariable
				options.value = data.currVal
				//noinspection JSUnresolvedFunction
				var chart = new JustGage(options)

				target.style.height = '207px'
				target.style.width = '325px'

				return chart
			}
		}
		var pie = {
			name: 'pie',
			isLegendSupported: true,
			canvasSupported: true,
			createChart: function setupPie (target, data, options) {
				var canvas = addCanvas(target)
				var canvasContext = canvas.getContext('2d')
				canvas.setAttribute('height', '150')
				canvas.setAttribute('width', '150')
				//noinspection JSUnresolvedFunction
				return new Chart(canvasContext).Pie(data, options)
			}
		}
		var line = {
			name: 'line',
			isLegendSupported: true,
			createChart: function setupLine (target, data, options) {
				var canvas = addCanvas(target)
				var canvasContext = canvas.getContext('2d')
				canvas.setAttribute('height', '200')
				canvas.setAttribute('width', '300')
				target.style.height = '207px'
				target.style.width = '325px'
				//noinspection JSUnresolvedFunction
				return new Chart(canvasContext).Line(data, options)
			}
		}
		var bar = {
			name: 'bar',
			isLegendSupported: true,
			createChart: function setupBar (target, data, options) {
				var canvas = addCanvas(target)
				var canvasContext = canvas.getContext('2d')
				canvas.setAttribute('height', '200')
				canvas.setAttribute('width', '300')
				canvas.setAttribute('data-xlabelrotation', '45')
				//noinspection JSUnresolvedFunction
				return new Chart(canvasContext).Bar(data, options)
			}
		}
		var radar = {
			name: 'radar',
			isLegendSupported: true,
			createChart: function setupRadar (target, data, options) {
				var canvas = addCanvas(target)
				var canvasContext = canvas.getContext('2d')
				canvas.setAttribute('height', '325')
				canvas.setAttribute('width', '400')
				//noinspection JSUnresolvedFunction
				return new Chart(canvasContext).Radar(data, options)
			}
		}
		var polarArea = {
			name: 'polar-area',
			isLegendSupported: true,
			createChart: function setupPolarArea (target, data, options) {
				var canvas = addCanvas(target)
				var canvasContext = canvas.getContext('2d')
				canvas.setAttribute('height', '325')
				canvas.setAttribute('width', '400')
				//noinspection JSUnresolvedFunction
				return new Chart(canvasContext).PolarArea(data, options)
			}
		}
		var types = [gauge, pie, line, bar, radar, polarArea]

		function getChartType (typeStr) {
			var chartType = null
			//noinspection JSUnusedLocalSymbols
			types.forEach(function (curr, idx, arr) {
				if (chartType == null && curr.name.toLowerCase() == typeStr.toLowerCase()) {
					chartType = curr
				}
			})
			return chartType
		}

		return {
			getType: getChartType
		}
	}()
	var options = JSON.parse(target.getAttribute('data-chart'))
	var data = JSON.parse(target.getAttribute('data-data'))
	var chartType = CHART_TYPES.getType(target.getAttribute('data-chart-type'))
	var includeLegend = target.getAttribute('data-include-legend').toLowerCase() == 'true'

	if (chartType != null) {
		target.chart = chartType.createChart(target, data, options)
		if (target.chart && includeLegend && chartType.isLegendSupported) {
			var legends = target.getElementsByClassName('chart-legend')
			var legend = null
			if (legends.length == 0) {
				legend = document.createElement('div')
				legend.className = 'chart-legend'
				target.appendChild(legend)
			}
			else {
				legend = legends[0]
			}
			//noinspection JSUnresolvedFunction
			legend.innerHTML = target.chart.generateLegend()
		}
	}
}