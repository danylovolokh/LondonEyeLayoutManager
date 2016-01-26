# LondonEyeLayoutManager

[![Android_weekly](https://img.shields.io/badge/Android%20Weekly-LondonEyeLayoutManager-green.svg)](http://androidweekly.net/issues/issue-183)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LondonEyeLayoutManager-green.svg?style=true)](https://android-arsenal.com/details/1/2917)

A LayoutManager that must be used with RecyclerView. 
When list is scrolled views are moved by circular trajectory


# London Eye
![alt tag](https://cloud.githubusercontent.com/assets/2686355/11732973/161e5970-9fb2-11e5-923b-09b6a0b4e26a.jpg)

# Requirements
android:minSdkVersion = 15

# Usage
```
List<String> mList = new ArrayList<>(Arrays.asList(
          "Passenger Cabin 1",
          "Passenger Cabin 2",
          "Passenger Cabin 3",
          "Passenger Cabin 4",
          "Passenger Cabin 5"));
            
  int screenWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
            
  // define circle radius
  int circleRadius = screenWidth;

  // define center of the circle
  int xOrigin = -200;
  int yOrigin = 0;
  mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
  
  // set radius and center
  mRecyclerView.setParameters(circleRadius, xOrigin, yOrigin);

  mLondonEyeLayoutManager = new LondonEyeLayoutManager(
          circleRadius,
          xOrigin,
          yOrigin,
          mRecyclerView,
          // define scroll strategy NATURAL / PIXEL_PERFECT
          IScrollHandler.Strategy.NATURAL);

  mRecyclerView.setLayoutManager(mLondonEyeLayoutManager);

  mVideoRecyclerViewAdapter = new YourCustomAdapter(getActivity(), mList);
  mRecyclerView.setAdapter(mVideoRecyclerViewAdapter);
```

#Details of implementation

[![Medium](https://img.shields.io/badge/Meduim-10%20steps%20to%20create%20a%20custom%20LayoutManager-blue.svg)](https://medium.com/@v.danylo/10-steps-to-create-a-custom-layoutmanager-2f30ab2f979d)

# Demo of Natural Scroll handler.

This scroll handler keeps the distance between cabins as a constant. This means that if your views are next to each other they will overlap each other when you scroll.

![alt tag](https://cloud.githubusercontent.com/assets/2686355/11742412/651bc71e-a008-11e5-9a5e-4f10be4adbd8.gif)

# Demo of PixelPerfect Scroll handler.

This scroll handler keeps views in touch. There is 2 rules:

1. View center is always on the circle
2. Views should touch each other either by top/bottom or by left/right sides.

Because we follow these rules we might see some unexpected behaviour:
If "view B" is below "view A" and views are scrolled down we can reach a point in which "view B" cannot longer stay below "view A" and keep it's center on the circle so, in this case "view B" jumps to the side in order to stay in touch with "view A" side by side and keep it's center on the circle.

![alt tag](https://cloud.githubusercontent.com/assets/2686355/11743339/4c1a8ffa-a00f-11e5-97f5-831b555b618d.gif)

# TODO:

1. Animations support.
2. Handle inPrelayout
3. Save/Restore instance state
4. Handle data set changes
5. Fix a crash when scrolling fast with Natural Scroll Handler

# Any contributions are welcome :)

# License

Copyright 2015 Danylo Volokh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

