# Free Food Finder

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Users post about free food they have leftover from events (or for whatever reason). Other users get push notifications when posts are created near their current location. Users can also browse posts on a stream or map view and filter posts by distance, allergens, and dietary restriction tags.

Video demonstration of the app is [here](https://www.youtube.com/watch?v=HCKPufC6TUo).

### App Evaluation
- **Category:** Food
- **Mobile:** uses location/map to tell you how far away the food is; camera for people to post a picture of the food; real-time notifications; poster can also use camera to show when food is gone
- **Story:** people like free food and people who have leftover food do not want it to go to waste. More streamlined than getting an email (also emails sometimes do not go out at the same time)
- **Market:** appeals mostly to people in cities or college students. Could also be used by companies who have free food promotions/samples and want to notify nearby people. Could also be used by restaurants at the end of the day when they have leftovers. Issue: people may be wary of the safety of the food
- **Habit:** people consume (go to get food) and create (post about free food). People will get notifications more frequently depending on what they set as their radius of where they're willing to go and where they're located. Companies who want to do promotions/organizations that frequently host events will likely post a lot.
- **Scope:** App is well defined and buildable within the given time (?). The basic feature of posting about free food/getting notified is pretty interesting to build.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can register a new account
* User can login
* User can view free food posts which indicate how close the food is and whether or not the food has been claimed
* User can open up Google maps to find a route to the free food
* User can post about free food, including a picture of the item and its location
* Users can mark when food has been fully claimed after posting
* User can open up camera from the app when posting food
* User can mark allergens the food they post contains
* User can filter posts on their feed/in notifications for dietary restrictions
* User can filter posts on their feed/in notifications for how far the food is and whether it's been claimed

**Optional Nice-to-have Stories**
* User can view the posts in their feed in a map view
* Users can comment on posts/rate the food
* Users can view all of their own past posts
* User can get real-time notifications when people near them post
* Poster gets a notification an hour after the posting asking if the food has been claimed if they haven't already market it as such
* User can edit their profile to include dietary restrictions
* Users can follow companies/organizations that post frequently
* Users can search for other users and view all of their past posts
* Users can customize whether or not others can view their past posts
* Users can customize notifications to only include those they follow
* Users can pay to have their posts show up at the top of people's feeds (i.e. sponsored posts)
* Users can earn awards for posting frequently or getting good reviews

### 2. Screen Archetypes
* Login
    * User can login
* Register
    * User can register a new account
* Stream
    * User can view free food posts which indicate how close the food is and whether or not the food has been claimed
* Map
    * User can view free food posts in a map view
* Detail
    * Users can mark when food has been fully claimed after posting
    * User can open up Google maps to find a route to the free food
* Creation
    * User can post about free food, including a picture of the item and its location
* Profile
    * User can view their own posts
* Settings
    * User can edit their account settings

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Map
* Creation
* Profile

**Flow Navigation** (Screen to Screen)

* Login
    * Stream
* Register
    * Stream
* Stream
    * Detail
* Map
    * Detail
* Detail
    * Stream
* Creation
    * Stream
* Profile
    * Detail
    * Settings
    * Login

## Wireframe
https://www.figma.com/file/u35L1rLYrGHbaMb4xBPz6Y/Wireframe?node-id=0%3A1
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>
![](https://i.imgur.com/Z5zAuvB.png)


## Schema
### Models

**Post**
| Property | Type | Description |
| -------- | ---- | ----------- |
| objectId | String | unique id for the user post (default field) |
| author | Pointer to User | post author |
| image | File | image that user posts |
| title | String | name of the food |
| location | JSON Object with "__type": "Geopoint" | location of the food
| description | String | more detailed description of the food |
| contains | JSON Object | maps various allergens/ingredients to a boolean (true if the food contains the allergen) |
| comments | Relation to Comment | all the comments that are on this post |
| claimed | Boolean | true if the food has been marked claimed, false otherwise |
| createdAt | DateTime | date when post is created (default field) |
| updatedAt | DateTime | date when post is last updated (default field) |

**User**
| Property | Type | Description |
| -------- | ---- | ----------- |
| objectId | String | unique id for the user (default field) |
| name | String | user's name |
| username | String | user's username |
| password | String | user's password |
| profileImage | File | user's profile image |
| posts | Relation to Post | all the posts that have been posted by this user |
| createdAt | DateTime | date when post is created (default field) |
| updatedAt | DateTime | date when post is last updated (default field) |

**Comment**
| Property | Type | Description |
| -------- | ---- | ----------- |
| objectId | String | unique id for the user (default field) |
| post | Pointer to Post | the post that the comment is on |
| author | Pointer to User | the author of the comment |
| image | File | image associated with the comment |
| description | String | comment by author |
| createdAt | DateTime | date when post is created (default field) |
| updatedAt | DateTime | date when post is last updated (default field) |


### Networking
* Login
    * (Read/GET) log in user
* Register
    * (Create/POST) create a new User
* Stream
    * (Read/GET) query all posts nearby
* Map
    * (Read/GET) query all posts nearby
* Post Detail
    * (Update/PUT) change claimed to true
    * (Update/PUT) change fields of post (title, image, location, description, contains)
    * (Delete) delete post
    * (Read/GET) query all comments on a post
    * (Create/POST) create a new comment on a post
* Creation
    * (Create/POST) create a new post object
* Profile
    * (Read/GET) query logged in user object
    * (Read/GET) query posts associated with user
* Settings
    * (Update/PUT) change various user fields
    * (Update/PUT) update user profile image


### APIs Used:
* Google Maps: used to display posts in a map view
* Google Places: used to allow users to search for a location by its street address
* Firebase: used to implement push notifications
