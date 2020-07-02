Original App Design Project - README Template
===

# Free Food Finder

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
Users make a post when they have leftover food from an event (or for whatever reason) and other users who are nearby are notified.

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

**Optional Nice-to-have Stories**
* User can get real-time notifications when people near them post
* User can open up camera from the app when posting food
* User can edit their profile to include dietary restrictions
* User can filter posts on their feed/in notifications for dietary restrictions
* User can mark allergens the food they post contains
* User can filter posts on their feed/in notifications for how far the food is and whether it's been claimed
* User can view the posts in their feed in a map view
* Users can comment on posts/rate the food
* Users can follow companies/organizations that post frequently
* Users can view all of their own past posts
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
* Detail
    * Users can mark when food has been fully claimed after posting
    * User can open up Google maps to find a route to the free food
* Creation
    * User can post about free food, including a picture of the item and its location

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Stream
* Creation
* (optional) Profile

**Flow Navigation** (Screen to Screen)

* Login
    * Stream
* Register
    * Stream
* Stream
    * Detail
    * (optional) Profile
* Detail
    * Stream
    * (optional) Profile
* Creation
    * Stream

## Wireframes
[Add picture of your hand sketched wireframes in this section]
<img src="YOUR_WIREFRAME_IMAGE_URL" width=600>

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema
[This section will be completed in Unit 9]
### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
