/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React from 'react';
import {StyleSheet, View, Text, TextInput, Button} from 'react-native';

import FloatingVideo from 'rn-float-video-widget';

const data = {
  video: {
    uri:
        "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
  }
};

export default class App extends React.Component {
  componentDidMount() {
    FloatingVideo.onClose(data => console.log(data));
  }

  render() {
    return (
      <View style={styles.container}>
        <Button
          title="Close Player"
          onPress={() => {
            FloatingVideo.Widget.close();
          }}
        />

        <View style={{flexDirection: 'row'}}>
          <Button
            title="Play Video"
            onPress={() => {
              FloatingVideo.open(data);
            }}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 0.6,
    justifyContent: 'space-around',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
