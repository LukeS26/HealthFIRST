# API Docs

## Account Endpoints
### Creating an account: `POST /api/account/signup`
Request body:
```json
{
    "username": "JohnSmith72",
    "first_name": "John",
    "last_name": "Smith",
    "email": "johnsmith@gmail.com",
    "password_hash": "testhash12345"
}
```

No response returned.

## Getting account information: `GET /api/profile/{USERNAME}`
No request body.

Response:
```json
{
    "username": "JohnSmith72",
    "first_name": "John",
    "last_name": "Smith",
    "email": "johnsmith@gmail.com",
    "biography": "Example biography, this can be an empty string ("")",
    "profile_picture_link": "https://LINK_TO_IMAGE/IMAGE_NAME.png",
    "permission_id": 0,
    "badge_ids": [1, 5, 7]
}
```
Response on error:
```json
{
    ""
}
```

## Creating a post: `POST /api/posts`
Request body:
```json
{
    "author": "JohnSmith72", // This will be changed later when authorization is implemented
    "title": "Example Title",
    "body": "Example body text"
}
```
No response returned.

## Getting a post: `GET /api/posts/{POST_ID}`
No request body.

Response:
```json
{
    "author": "JohnSmith72",
    "title": "Example Title",
    "body": "Example body text",
    "comments": [["A0", ["B0", ["C0"], ["C1"]], ["B1"]], ["A1", ["B0", ["C0"]], ["B1"]]]
}
```