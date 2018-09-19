/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */
$.ajaxSetup({async: false});
$.date = function (dateObject) {
    var d = new Date(dateObject);
    var day = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    if (day < 10) {
        day = "0" + day;
    }
    if (month < 10) {
        month = "0" + month;
    }
    return day + "/" + month + "/" + year;
};
$(function () {

    var filemanager = $('.filemanager'),
        breadcrumbs = $('.breadcrumbs'),
        fileList = filemanager.find('.data');

    var pToggle = $('.proptoggle');
    pToggle.on('click', function () {
        $('.propdetails').toggle();
        pToggle.find('i').toggleClass("icono-caretDownSquare icono-caretUpSquare");
    });

    var drop = $('#drop');
    var upload = $('#upload');

    var dToggle = $('.droptoggle');
    dToggle.on('click', function () {
        upload.toggle();
    });

    // setup file drop zone

    var ul = upload.find('ul');

    drop.find('a').click(function () {
        // Simulate a click on the file input button
        // to show the file browser dialog
        $(this).parent().find('input').click();
    });


    // Prevent the default action when a file is dropped on the window
    $(document).on('drop dragover', function (e) {
        e.preventDefault();
    });



    // Start by fetching the file data from scan.php with an AJAX request

    $.get('scan2?dir=root', function (data) {
        $('#folderid').val("/");
        var response = data,
            currentPath = '',
            breadcrumbsUrls = [];

        var selectedId = null;

        var folders = [],
            files = [];


        //------file drop / upload -------


        function refreshPage() {
            render(searchByPath(currentPath));
        }


        // Initialize the jQuery File Upload plugin
        upload.fileupload({

            // This element will accept file drag/drop uploading
            dropZone: drop,

            // This function is called when a file is added to the queue;
            // either via the browse button, or via drag/drop:
            add: function (e, data) {

                var tpl = $('<li class="working"><input type="text" value="0" data-width="48" data-height="48"' +
                    ' data-fgColor="#0788a5" data-readOnly="1" data-bgColor="#3e4043" /><p></p><span></span></li>');

                // Append the file name and file size
                tpl.find('p').text(data.files[0].name)
                    .append('<i>' + formatFileSize(data.files[0].size) + '</i>');

                // Add the HTML to the UL element
                data.context = tpl.appendTo(ul);

                // Initialize the knob plugin
                tpl.find('input').knob();

                // Listen for clicks on the cancel icon
                tpl.find('span').click(function () {

                    if (tpl.hasClass('working')) {
                        jqXHR.abort();
                    }

                    tpl.fadeOut(function () {
                        tpl.remove();
                    });

                });

                // Automatically upload the file once it is added to the queue
                var jqXHR = data.submit();
            },

            progress: function (e, data) {

                // Calculate the completion percentage of the upload
                var progress = parseInt(data.loaded / data.total * 100, 10);

                // Update the hidden input field and trigger a change
                // so that the jQuery knob plugin knows to update the dial
                data.context.find('input').val(progress).change();

                if (progress == 100) {
                    data.context.removeClass('working');
                    refreshPage();
                }
            },

            fail: function (e, data) {
                // Something has gone wrong!
                data.context.addClass('error');
            }
        });

        // Helper function that formats the file sizes
        function formatFileSize(bytes) {
            if (typeof bytes !== 'number') {
                return '';
            }

            if (bytes >= 1000000000) {
                return (bytes / 1000000000).toFixed(2) + ' GB';
            }

            if (bytes >= 1000000) {
                return (bytes / 1000000).toFixed(2) + ' MB';
            }

            return (bytes / 1000).toFixed(2) + ' KB';
        }

        //END------file drop / upload -------





        //reset highlighting on background click
        //$('.filemanager').on("click", function () {
        //    $(".highlight").toggleClass("highlight", false);
        //    $(".propdetails").text('');
        //    selectedId = null
        //});

        // This event listener monitors changes on the URL. We use it to
        // capture back/forward navigation in the browser.

        $(window).on('hashchange', function () {

            goto(window.location.hash);

            // We are triggering the event. This will execute
            // this function on page load, so that we show the correct folder:

        }).trigger('hashchange');


        // Hiding and showing the search box

        filemanager.find('.search').click(function () {

            var search = $(this);

            search.find('span').hide();
            search.find('input[type=search]').show().focus();

        });


        // Listening for keyboard input on the search field.
        // We are using the "input" event which detects cut and paste
        // in addition to keyboard input.

        filemanager.find('input').on('input', function (e) {

            folders = [];
            files = [];

            var value = this.value.trim();

            if (value.length) {

                filemanager.addClass('searching');

                // Update the hash on every key stroke
                window.location.hash = 'search=' + value.trim();

            }

            else {

                filemanager.removeClass('searching');
                window.location.hash = encodeURIComponent(currentPath);

            }

        }).on('keyup', function (e) {

            // Clicking 'ESC' button triggers focusout and cancels the search

            var search = $(this);

            if (e.keyCode == 27) {

                search.trigger('focusout');

            }

        }).focusout(function (e) {

            // Cancel the search

            var search = $(this);

            if (!search.val().trim().length) {

                window.location.hash = encodeURIComponent(currentPath);
                search.hide();
                search.parent().find('span').show();

            }

        });


        //Clicking on Files

        fileList.on('click', 'li.files', function (e) {
            var objectid = $(this).find('a').attr('data-target');
            if (objectid == selectedId) {
                return true;
            } else {
                e.preventDefault();
                $(".highlight").toggleClass("highlight", false);
                $(this).toggleClass("highlight", true);
                loadDetails(objectid);
                return false;
            }
        });

        // Clicking on folders
        fileList.on('click', 'li.folders', function (e) {
            var objectid = $(this).find('a').attr('data-target');
            e.preventDefault();
            if (objectid == selectedId) {
                $(this).trigger("dblclick");
            } else {
                $(".highlight").toggleClass("highlight", false);
                $(this).toggleClass("highlight", true);
                loadDetails(objectid)
            }
            return false;
        });

        fileList.on('dblclick', 'li.folders', function (e) {
            e.preventDefault();
            $(".propdetails").text('');
            var nextDir = $(this).find('a.folders').attr('href');

            if (filemanager.hasClass('searching')) {

                // Building the breadcrumbs

                breadcrumbsUrls = generateBreadcrumbs(nextDir);

                filemanager.removeClass('searching');
                filemanager.find('input[type=search]').val('').hide();
                filemanager.find('span').show();
            }
            else {
                breadcrumbsUrls.push(nextDir);
            }

            window.location.hash = encodeURIComponent(nextDir);
            currentPath = nextDir;
        });


        // Clicking on breadcrumbs

        breadcrumbs.on('click', 'a', function (e) {
            e.preventDefault();

            var index = breadcrumbs.find('a').index($(this)),
                nextDir = breadcrumbsUrls[index];

            breadcrumbsUrls.length = Number(index);

            window.location.hash = encodeURIComponent(nextDir);

        });

        //load the details

        function loadDetails(objectid) {
            //var detailsEnabled = $('.proptoggle i').hasClass('icono-caretUpSquare');
            if (objectid != undefined && selectedId != objectid) {
                // console.log("get details for " + objectid);
                $.get('details?id=' + objectid, function (data) {

                    $(".propdetails").text('').append('<table></table>');
                    $.each(data, function (idx, prop) {
                        var name = prop['displayName'];
                        var values = prop['values'];
                        if (name.indexOf("Date") > -1) {
                            try {
                                values = $.date(values[0]);
                            } catch (err) {
                            }
                        }
                        $(".propdetails").find('table').append('<tr><td>' + name + '</td><td>' + values + '</td></tr>');
                    });
                });
                selectedId = objectid;
            }

        }


        // Navigates to the given hash (path)

        function goto(hash) {

            hash = decodeURIComponent(hash).slice(1).split('=');

            if (hash.length) {
                var rendered = '';

                // if hash has search in it

                if (hash[0] === 'search') {

                    filemanager.addClass('searching');
                    rendered = searchData(response, hash[1].toLowerCase());

                    if (rendered.length) {
                        currentPath = hash[0];
                        render(rendered);
                    }
                    else {
                        render(rendered);
                    }

                }

                // if hash is some path

                else if (hash[0].trim().length) {

                    rendered = searchByPath(hash[0]);

                    if (rendered.length) {

                        currentPath = hash[0];
                        breadcrumbsUrls = generateBreadcrumbs(hash[0]);
                        render(rendered);

                    }
                    else {
                        currentPath = hash[0];
                        breadcrumbsUrls = generateBreadcrumbs(hash[0]);
                        render(rendered);
                    }

                }

                // if there is no hash

                else {
                    //currentPath = data.path;
                    currentPath = "root";
                    breadcrumbsUrls.push(currentPath);
                    render(searchByPath(currentPath));
                }
            }
        }

        // Splits a file path and turns it into clickable breadcrumbs

        function generateBreadcrumbs(nextDir) {
            var path = nextDir.split('/').slice(0);
            for (var i = 1; i < path.length; i++) {
                path[i] = path[i - 1] + '/' + path[i];
            }
            return path;
        }


        // Locates a file by path

        function searchByPath(dir) {
            // console.log("search: " + dir)
            $.get('scan2?dir=' + dir, function (data) {
                response = data
                $('#folderid').val(dir);
            });
            return response;
        }


        // Recursively search through the file tree

        function searchData(data, searchTerms) {

            data.forEach(function (d) {
                //  console.log(d)
                if (d.type === 'folder') {

                    //searchData(d.items, searchTerms);

                    if (d.name.toLowerCase().match(searchTerms)) {
                        folders.push(d);
                    }
                }
                else if (d.type === 'file') {
                    if (d.name.toLowerCase().match(searchTerms)) {
                        files.push(d);
                    }
                }
            });
            return {folders: folders, files: files};
        }


        // Render the HTML for the file manager

        function render(data) {
            // console.log("render " + data)
            var scannedFolders = [],
                scannedFiles = [];

            if (Array.isArray(data)) {

                data.forEach(function (d) {

                    if (d.type === 'folder') {
                        scannedFolders.push(d);
                    }
                    else if (d.type === 'file') {
                        scannedFiles.push(d);
                    }

                });

            }
            else if (typeof data === 'object') {

                scannedFolders = data.folders;
                scannedFiles = data.files;

            }


            // Empty the old result and make the new one

            fileList.empty().hide();

            if (!scannedFolders.length && !scannedFiles.length) {
                filemanager.find('.nothingfound').show();
            }
            else {
                filemanager.find('.nothingfound').hide();
            }

            if (scannedFolders.length) {

                scannedFolders.forEach(function (f) {

                    var itemsLength = f.items,
                        name = escapeHTML(f.name),
                        icon = '<span class="icon folder"></span>';

                    if (itemsLength) {
                        icon = '<span class="icon folder full"></span>';
                    }

                    if (itemsLength == 1) {
                        itemsLength += ' item';
                    }
                    else if (itemsLength > 1) {
                        itemsLength += ' items';
                    }
                    else {
                        itemsLength = 'Empty';
                    }

                    var folder = $('<li class="folders"><a href="' + f.path + '" title="' + f.path + '" class="folders" data-target="' + f.id + '">' + icon + '<span class="name">' + name + '</span> <span class="details">' + itemsLength + '</span></a></li>');
                    folder.appendTo(fileList);
                });

            }

            if (scannedFiles.length) {

                scannedFiles.forEach(function (f) {

                    var fileSize = bytesToSize(f.size),
                        name = escapeHTML(f.name),
                        fileType = name.split('.'),
                        icon = '<span class="icon file"></span>';

                    fileType = fileType[fileType.length - 1];

                    icon = '<span class="icon file f-' + fileType + '">.' + fileType + '</span>';

                    var file = $('<li class="files"><a target="_blank" href="/browser/' + f.repoid + '/root?objectid=' + f.id + '&cmisselector=content' + '" title="' + f.path + '" class="files"  data-target="' + f.id + '">' + icon + '<span class="name">' + name + '</span> <span class="details">' + fileSize + '</span></a></li>');
                    file.appendTo(fileList);
                });

            }
            // Generate the breadcrumbs

            var url = '';

            if (filemanager.hasClass('searching')) {

                url = '<span>Search results: </span>';
                fileList.removeClass('animated');

            }
            else {

                fileList.addClass('animated');

                breadcrumbsUrls.forEach(function (u, i) {

                    var name = u.split('/');

                    if (i !== breadcrumbsUrls.length - 1) {
                        url += '<a href="' + u + '"><span class="folderName">' + name[name.length - 1] + '</span></a> <span class="arrow">→</span> ';
                    }
                    else {
                        url += '<span class="folderName">' + name[name.length - 1] + '</span>';
                    }

                });

            }

            breadcrumbs.text('').append(url);


            // Show the generated elements

            fileList.animate({'display': 'inline-block'});

        }


        // This function escapes special html characters in names

        function escapeHTML(text) {
            return text.replace(/\&/g, '&amp;').replace(/\</g, '&lt;').replace(/\>/g, '&gt;');
        }


        // Convert file sizes from bytes to human readable units

        function bytesToSize(bytes) {
            var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
            if (bytes == 0) return '0 Bytes';
            var i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)));
            return Math.round(bytes / Math.pow(1024, i), 2) + ' ' + sizes[i];
        }

    });
});