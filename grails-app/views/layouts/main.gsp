<!doctype html>
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
		<title><g:layoutTitle default="Grails"/></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon">
		<link rel="apple-touch-icon" href="${resource(dir: 'images', file: 'apple-touch-icon.png')}">
		<link rel="apple-touch-icon" sizes="114x114" href="${resource(dir: 'images', file: 'apple-touch-icon-retina.png')}">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}" type="text/css">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'mobile.css')}" type="text/css">

        <g:javascript library="jquery" />
        <g:javascript>
            function showSpinner(){
                $("#spinner").show();
            }
            function hideSpinner(){
                $("#spinner").hide();
            }

            $(document).ready(function() {
                var text = "Quicksearch";
                if($('#quicksearchbox').attr("value") != text){
                    $('#quicksearchbox').addClass("active");
                }

                $("#quicksearchbox").focus(function() {
                    $(this).addClass("active");
                    if($(this).attr("value") == text) $(this).attr("value", "");
                });

                $("#quicksearchbox").blur(function() {
                    $(this).removeClass("active");
                    if($(this).attr("value") == "") $(this).attr("value", text);
                });
            });
        </g:javascript>
        <g:layoutHead/>
		<r:layoutResources />
	</head>
	<body>
		<div id="header" role="banner">
            <div class="main-logo"></div>
            <div class="navigation">
                <sec:ifLoggedIn>
                    <ul>
                        <li><g:link controller="job" action="list">Jobs</g:link></li>
                        <li><g:link controller="cluster" action="index">Cluster</g:link></li>
                        <li><g:link controller="user" action="show" id="${sec.loggedInUserInfo(field:'id')}">Preferences</g:link></li>
                        <sec:ifAnyGranted roles="ROLE_ADMIN">
                            <li><g:link controller="user" action="list" id="">Users</g:link></li>
                        </sec:ifAnyGranted>
                    </ul>
                    <div class="logout">
                        <g:form name='quicksearch' method="get" action="list" controller="job">
                            <g:textField id="quicksearchbox" name="q" value="${params.q ? params.q : 'Quicksearch'}"/>
                        </g:form>
                        <g:link controller="logout">Logout</g:link>
                    </div>
                </sec:ifLoggedIn>
            </div>

        </div>
        <div class="clear"></div>

		<g:layoutBody/>
		<div class="footer" role="contentinfo">
            Jip Pipeline System @ 2012
		</div>
        <div id="dialog"></div>
        <div id="spinner"></div>
		<g:javascript library="application"/>
		<r:layoutResources />
	</body>
</html>