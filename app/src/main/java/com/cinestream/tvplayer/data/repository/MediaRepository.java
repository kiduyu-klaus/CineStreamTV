package com.cinestream.tvplayer.data.repository;

import com.cinestream.tvplayer.data.model.MediaItems;

import java.util.ArrayList;
import java.util.List;

public class MediaRepository {

    public List<MediaItems> getFeaturedMovies() {
        List<MediaItems> movies = new ArrayList<>();
        
        // Sample Big Buck Bunny - HLS
        MediaItems bigBuckBunny = new MediaItems();
        bigBuckBunny.setId("1");
        bigBuckBunny.setTitle("Big Buck Bunny");
        bigBuckBunny.setDescription("A large and lovable rabbit deals with three bullying rodents: Frank, Rinky, and Gamera.");
        bigBuckBunny.setPosterUrl("https://peach.blender.org/wp-content/uploads/title_anouncement.jpg?x11217");
        bigBuckBunny.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        bigBuckBunny.setHlsUrl("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8");
        bigBuckBunny.setDuration("9:56");
        bigBuckBunny.setYear(2008);
        bigBuckBunny.setGenre("Animation");
        bigBuckBunny.setRating(4.2f);
        movies.add(bigBuckBunny);
        
        // Sample Tears of Steel - 4K
        MediaItems tearsOfSteel = new MediaItems();
        tearsOfSteel.setId("2");
        tearsOfSteel.setTitle("Tears of Steel");
        tearsOfSteel.setDescription("A group of warriors and scientists gather at the Old Delft to make their last stand against a powerful alien.");
        tearsOfSteel.setPosterUrl("https://mango.blender.org/wp-content/uploads/2013/05/TOSPosterwide.jpg");
        tearsOfSteel.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4");
        tearsOfSteel.setHlsUrl("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8");
        tearsOfSteel.setDuration("12:14");
        tearsOfSteel.setYear(2012);
        tearsOfSteel.setGenre("Sci-Fi");
        tearsOfSteel.setRating(4.5f);
        movies.add(tearsOfSteel);
        
        return movies;
    }

    public List<MediaItems> getActionMovies() {
        List<MediaItems> movies = new ArrayList<>();
        
        // Sample Sintel
        MediaItems sintel = new MediaItems();
        sintel.setId("3");
        sintel.setTitle("Sintel");
        sintel.setDescription("A lonely young woman, Sintel, helps and befriends a dragon, whom she calls Scales.");
        sintel.setPosterUrl("https://mango.blender.org/wp-content/uploads/2013/05/Sintel_poster.png");
        sintel.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4");
        sintel.setHlsUrl("https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8");
        sintel.setDuration("14:48");
        sintel.setYear(2010);
        sintel.setGenre("Adventure");
        sintel.setRating(4.7f);
        movies.add(sintel);
        
        // Sample for different quality testing
        MediaItems testVideo = new MediaItems();
        testVideo.setId("4");
        testVideo.setTitle("Test Video (Multi Quality)");
        testVideo.setDescription("A test video with multiple quality options for demonstration.");
        testVideo.setPosterUrl("https://images.unsplash.com/photo-1536240478700-b869070f9279?w=500");
        testVideo.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
        testVideo.setHlsUrl("https://multiplatform-f.akamaihd.net/i/multi/will/bunny/big_buck_bunny_,640x360_450_b,640x360_700_b,640x360_1000_b,848x480_1500_b,.f4v.csmil/master.m3u8");
        testVideo.setDuration("9:56");
        testVideo.setYear(2008);
        testVideo.setGenre("Test");
        testVideo.setRating(4.0f);
        movies.add(testVideo);
        
        // MP4 Format Sample
        MediaItems mp4Sample = new MediaItems();
        mp4Sample.setId("9");
        mp4Sample.setTitle("MP4 Sample Video");
        mp4Sample.setDescription("Demonstration of MP4 format support with high quality playback.");
        mp4Sample.setPosterUrl("https://images.unsplash.com/photo-1485846234645-a62644f84728?w=500");
        mp4Sample.setVideoUrl("https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_1mb.mp4");
        mp4Sample.setDuration("0:30");
        mp4Sample.setYear(2023);
        mp4Sample.setGenre("Demo");
        mp4Sample.setRating(4.2f);
        movies.add(mp4Sample);
        
        return movies;
    }

    public List<MediaItems> getComedyMovies() {
        List<MediaItems> movies = new ArrayList<>();
        
        MediaItems forBiggerJoyrides = new MediaItems();
        forBiggerJoyrides.setId("5");
        forBiggerJoyrides.setTitle("For Bigger Joyrides");
        forBiggerJoyrides.setDescription("A fun ride with our friends for bigger adventures.");
        forBiggerJoyrides.setPosterUrl("https://i.imgur.com/kZc3Qzg.png");
        forBiggerJoyrides.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4");
        forBiggerJoyrides.setDuration("15:32");
        forBiggerJoyrides.setYear(2013);
        forBiggerJoyrides.setGenre("Comedy");
        forBiggerJoyrides.setRating(3.8f);
        movies.add(forBiggerJoyrides);
        
        MediaItems forBiggerEscapes = new MediaItems();
        forBiggerEscapes.setId("6");
        forBiggerEscapes.setTitle("For Bigger Escapes");
        forBiggerEscapes.setDescription("Escape to a world of laughter and adventure.");
        forBiggerEscapes.setPosterUrl("https://i.imgur.com/ZrOc4oZ.png");
        forBiggerEscapes.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4");
        forBiggerEscapes.setDuration("15:32");
        forBiggerEscapes.setYear(2013);
        forBiggerEscapes.setGenre("Comedy");
        forBiggerEscapes.setRating(3.9f);
        movies.add(forBiggerEscapes);
        
        return movies;
    }

    public List<MediaItems> getDramaMovies() {
        List<MediaItems> movies = new ArrayList<>();
        
        MediaItems forBiggerMeltdowns = new MediaItems();
        forBiggerMeltdowns.setId("7");
        forBiggerMeltdowns.setTitle("For Bigger Meltdowns");
        forBiggerMeltdowns.setDescription("An emotional journey through life's challenges.");
        forBiggerMeltdowns.setPosterUrl("https://i.imgur.com/aKBC1.png");
        forBiggerMeltdowns.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4");
        forBiggerMeltdowns.setDuration("15:32");
        forBiggerMeltdowns.setYear(2013);
        forBiggerMeltdowns.setGenre("Drama");
        forBiggerMeltdowns.setRating(4.1f);
        movies.add(forBiggerMeltdowns);
        
        return movies;
    }

    public List<MediaItems> getDocumentaries() {
        List<MediaItems> movies = new ArrayList<>();
        
        MediaItems elephantsDream = new MediaItems();
        elephantsDream.setId("8");
        elephantsDream.setTitle("Elephants Dream");
        elephantsDream.setDescription("The story of two strange characters exploring a capricious world.");
        elephantsDream.setPosterUrl("https://mango.blender.org/wp-content/uploads/2013/05/poster_elephants.jpg");
        elephantsDream.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4");
        elephantsDream.setHlsUrl("https://demo.unified-streaming.com/k8s/features/stable/video/tears-of-steel/tears-of-steel.ism/.m3u8");
        elephantsDream.setDuration("10:53");
        elephantsDream.setYear(2006);
        elephantsDream.setGenre("Documentary");
        elephantsDream.setRating(4.3f);
        movies.add(elephantsDream);
        
        return movies;
    }

    public List<MediaItems> getFormatDemos() {
        List<MediaItems> movies = new ArrayList<>();
        
        // MKV Format Demo
        MediaItems mkvDemo = new MediaItems();
        mkvDemo.setId("10");
        mkvDemo.setTitle("MKV Format Demo");
        mkvDemo.setDescription("Demonstration of MKV (Matroska) format support with multiple audio tracks.");
        mkvDemo.setPosterUrl("https://images.unsplash.com/photo-1518709268805-4e9042af2176?w=500");
        mkvDemo.setVideoUrl("https://file-examples.com/storage/fe86c3a18f77c8e6a2e9f0f/2017/10/file_example_MKV_480x900_1MG.mkv");
        mkvDemo.setDuration("0:15");
        mkvDemo.setYear(2023);
        mkvDemo.setGenre("Format Demo");
        mkvDemo.setRating(4.0f);
        movies.add(mkvDemo);
        
        // WebM Format Demo
        MediaItems webmDemo = new MediaItems();
        webmDemo.setId("11");
        webmDemo.setTitle("WebM Format Demo");
        webmDemo.setDescription("Demonstration of WebM format support for web-optimized streaming.");
        webmDemo.setPosterUrl("https://images.unsplash.com/photo-1518832553480-cd0e625ed3e6?w=500");
        webmDemo.setVideoUrl("https://sample-videos.com/zip/10/webm/SampleVideo_1280x720_1mb.webm");
        webmDemo.setDuration("0:30");
        webmDemo.setYear(2023);
        webmDemo.setGenre("Format Demo");
        webmDemo.setRating(4.1f);
        movies.add(webmDemo);
        
        // High Quality MP4
        MediaItems highQualityMP4 = new MediaItems();
        highQualityMP4.setId("12");
        highQualityMP4.setTitle("High Quality MP4");
        highQualityMP4.setDescription("High-bitrate MP4 file demonstrating superior video quality.");
        highQualityMP4.setPosterUrl("https://images.unsplash.com/photo-1518709594023-6eab9bab7b23?w=500");
        highQualityMP4.setVideoUrl("https://sample-videos.com/zip/10/mp4/SampleVideo_1280x720_5mb.mp4");
        highQualityMP4.setDuration("1:00");
        highQualityMP4.setYear(2023);
        highQualityMP4.setGenre("Format Demo");
        highQualityMP4.setRating(4.5f);
        movies.add(highQualityMP4);
        
        return movies;
    }
}