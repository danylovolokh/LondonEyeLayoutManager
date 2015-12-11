# LondonEyeLayoutManager

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

# Demo of Natural Scroll handler.

This scroll handler keeps the distance between cabins as a constant. This means that if your views are next to each other they will overlap each other when you scroll.

![alt tag](https://cloud.githubusercontent.com/assets/2686355/11742412/651bc71e-a008-11e5-9a5e-4f10be4adbd8.gif)
