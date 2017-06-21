<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<title>Blunderful Online Oppurtunities Management, Inc</title>
</head>
<body>

<h2>Account Details</h2>

<p>
	Username: ${userDetails.firstName} <br/>
	First Name: ${userDetails.firstName} <br/>
	Last Name: ${userDetails.lastName} <br/>
	Phone:  ${userDetails.phone} <br/>
	Desired Salary: $${userDetails.salary} <br/>
	After Increase: $${userDetails.increase}
</p>

<form action="account">
	<input type="hidden" name="action" value="logout"/>
	<input type="submit" value="Logout"/>
</form>

</body>
</html>