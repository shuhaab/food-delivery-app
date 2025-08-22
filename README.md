# Java Assignment
The controllers are located in [src/main/java/com/g25](src/main/java/com/g25)  
The FXML (layout) and CSS (styling) files are located in [src/main/resources/com/g25](src/main/resources/com/g25)  
  
The JSON files are located in [src/main/resources/com/g25/db](src/main/resources/com/g25/db)  
The Java classes for the JSON objects are located in [src/main/java/com/g25/db](src/main/java/com/g25/db)  
  
Before doing any changes, make sure to fetch and pull the latest changes from the repository. You can do this by running the following commands in your terminal (in order):  
```bash
git fetch
git pull
```
Adding a new user to the users JSON file example:  
```java
Users users = new Users();
JSONArray userArray = users.getData();

// create a new user by duplicating the first user object
JSONObject newUser = new JSONObject(userArray.getJSONObject(0).toString());
// change the name to the new user's name
newUser.put("name", "dude");
// add the new user to the users array
userArray.put(newUser);

// update the data
users.setData(userArray);
// update users.txt
users.updateContent();
```
