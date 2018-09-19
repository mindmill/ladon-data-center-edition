/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */


(function ($) {
    "use strict";
    var mainApp = {

        initFunction: function () {
            /*MENU 
             ------------------------------------*/
            $('#main-menu').metisMenu();

            $(window).bind("load resize", function () {
                if ($(this).width() < 768) {
                    $('div.sidebar-collapse').addClass('collapse')
                } else {
                    $('div.sidebar-collapse').removeClass('collapse')
                }
            });

            $('#objectForm_objectid').click(function () {
                $('#objectForm_token').val('')
            });


            if (typeof ladon !== 'undefined' &&
                typeof ladon.statisticdata !== 'undefined') {
                var g1 = new JustGage({
                    id: "readGauge",
                    value: ladon.statisticdata.reads,
                    min: 0,
                    max: ladon.statisticdata.maxReads,
                    title: "Reads",
                    label: "objects/min",
                    gaugeWidthScale: 0.3
                });
                var g2 = new JustGage({
                    id: "writeGauge",
                    value: ladon.statisticdata.writes,
                    min: 0,
                    max: ladon.statisticdata.maxWrites,
                    title: "Writes",
                    label: "objects/min",
                    gaugeWidthScale: 0.3
                });
                var g3 = new JustGage({
                    id: "readDataGauge",
                    value: ladon.statisticdata.chunkReads,
                    min: 0,
                    max: ladon.statisticdata.maxChunkReads,
                    title: "Reads",
                    label: "chunks/min",
                    gaugeWidthScale: 0.3
                });
                var g4 = new JustGage({
                    id: "writeDataGauge",
                    value: ladon.statisticdata.chunkWrites,
                    min: 0,
                    max: ladon.statisticdata.maxChunkWrites,
                    title: "Writes",
                    label: "chunks/min",
                    gaugeWidthScale: 0.3
                });

                /*
                 setInterval($.getScript, 5000, './statsload', function () {

                 }
                 );*/

                var sload = setInterval(function () {
                    //if(ladon.statisticdata.refreshRepoStats == true){
                    //  $("#refreshRepoStats").load("statistics.htm #reload")

                    $.get("statistics", function (result) {
                        result = $(result);
                        if (ladon.statisticdata.refreshRepoStats == true) {
                            $("#refreshRepoStats").html(result.find('#reload'));
                        }
                        result.find('script').appendTo('#refreshRepoStats');
                    }, 'html');

                    g1.config.max = ladon.statisticdata.maxReads;
                    g1.refresh(ladon.statisticdata.reads);
                    g2.config.max = ladon.statisticdata.maxWrites;
                    g2.refresh(ladon.statisticdata.writes);
                    g3.config.max = ladon.statisticdata.maxChunkReads;
                    g3.refresh(ladon.statisticdata.chunkReads);
                    g4.config.max = ladon.statisticdata.maxChunkWrites;
                    g4.refresh(ladon.statisticdata.chunkWrites);


                    // }else{
                    // clearInterval(sload)
                    //}
                }, 5000)
            }


            /* MORRIS JVM CHART
             ----------------------------------------*/

            if (typeof ladon !== 'undefined' &&
                typeof ladon.jvmtext !== 'undefined') {



                /* MORRIS AREA CHART
                 ----------------------------------------*/

                var cpuGraph = Morris.Area({
                    element: 'morris-area-chart',
                    data: ladon.cpuinfo,
                    behaveLikeLine: true,
                    xkey: 'time',
                    ykeys: ['system', 'jvm'],
                    labels: ['System', 'Ladon'],
                    pointSize: 1,
                    hideHover: 'auto',
                    resize: false
                });

                /* MORRIS AREA CHART
                 ----------------------------------------*/

                var heapGraph = Morris.Area({
                    element: 'morris-heap-chart',
                    data: ladon.meminfo,
                    behaveLikeLine: true,
                    xkey: 'time',
                    ykeys: ['total', 'free'],
                    labels: ['Total', 'Free'],
                    pointSize: 1,
                    hideHover: 'auto',
                    resize: false
                });

                /* MORRIS BAR CHART
                 -----------------------------------------*/
                var fsGraph = Morris.Bar({
                    element: 'morris-fs-chart',
                    data: ladon.fsinfo,
                    xkey: 'y',
                    ykeys: ['a', 'b'],
                    labels: ['Used', 'Total'],
                    hideHover: false,
                    resize: true
                });
                $.getScript('./cpuload', function () {
                    cpuGraph.setData(ladon.cpuinfo);
                    heapGraph.setData(ladon.meminfo);
                    $.getScript('./cpuload', function () {
                        cpuGraph.setData(ladon.cpuinfo);
                        heapGraph.setData(ladon.meminfo);
                    });
                });
                setInterval($.getScript, 5000, './cpuload', function () {
                        cpuGraph.setData(ladon.cpuinfo);
                        heapGraph.setData(ladon.meminfo);
                        $('#freeMem').text(ladon.jvmtext.pop());
                        $('#totalMem').text(ladon.jvmtext.pop())
                    }
                );
                setInterval(function () {
                    fsGraph.setData(ladon.fsinfo)
                }, 30000)
            }
        },

        initialization: function () {
            mainApp.initFunction();

        }

    };
    // Initializing ///

    $(document).ready(function () {
        var oTable = $('#dataTables-repositories').dataTable();
        $('#dataTables-props').dataTable();
        $('#dataTables-props2').dataTable();
        $('#dataTables-logs').dataTable();

        $('#newtypexml').each(function () {

            var editor = CodeMirror.fromTextArea(this, {
                mode: "xml",
                lineNumbers: true,
                extraKeys: {
                    "Ctrl-Space": "autocomplete",
                    "F11": function (cm) {
                        cm.setOption("fullScreen", !cm.getOption("fullScreen"));
                    },
                    "Esc": function (cm) {
                        if (cm.getOption("fullScreen")) cm.setOption("fullScreen", false);
                    }
                }
            });
            editor.setSize("100%", "60%");
            $('#errorline').each(function () {
                var line = $(this).html();
                //  console.log(line);
                editor.addLineClass(parseInt(line) - 1, 'background', 'line-error');
            });


        });

        mainApp.initFunction();

        window.prettyPrint && prettyPrint()

    });

}(jQuery));
