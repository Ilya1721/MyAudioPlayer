package test;
import javafx.scene.media.*;
import java.io.*;
import java.nio.file.Paths;
import javafx.application.*;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.event.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.geometry.*;
import javafx.beans.value.*;
import javafx.util.*;
import javafx.collections.*;
import java.util.*;

public class MyAudioPlayer extends Application
{	
	private Media hit;
	private MediaPlayer player;
	private File track;
	private Label track_slider_name;
	private Label track_current_pos;
	private Slider track_slider;
	private Slider volume;
	private Label track_name;
	private ObservableList<String> track_list;
	private ObservableMap<String, File> file_list;
	private ListView<String> track_view;
	private MultipleSelectionModel<String> sel_model;
	private FileChooser file_chooser;
	private TextField search_tracks;
	private ChangeListener<Number> track_listener;
	
	public void start(Stage my_stage) 
	{
		file_chooser = new FileChooser();
		VBox root_node = new VBox();
		FlowPane button_layout = new FlowPane();
		FlowPane volume_layout = new FlowPane();
		FlowPane track_name_layout = new FlowPane();
		FlowPane track_slider_layout = new FlowPane();
		FlowPane tracks_button_layout = new FlowPane();
		FlowPane save_load_layout = new FlowPane();
		FlowPane next_buttons_layout = new FlowPane();
		FlowPane shuffle_buttons = new FlowPane();
		Scene my_scene = new Scene(root_node, 500, 400);
		Button play = new Button("Play");
		Button stop = new Button("Stop");
		Button get_track = new Button("Get Track");
		Button add_track = new Button("Add Track");
		Button play_tracks = new Button("Play list");
		Button delete_track = new Button("Delete track");
		Button save = new Button("Save");
		Button load = new Button("Load");
		Button next_track = new Button("Next track");
		Button prev_track = new Button("Prev track");
		Button shuffle_tracks = new Button("Shuffle");
		Button sort_tracks = new Button("Sort");
		Button search_button = new Button("Search");
		search_tracks = new TextField("Search the tracks here");
		final ContextMenu sort_menu = new ContextMenu();
		MenuItem alphabetic_sort = new MenuItem("Alphabetic sort");
		MenuItem reverse_alpha_sort = new MenuItem("Reverse alpha sort");
		volume = new Slider();
		track_slider = new Slider();
		Label volume_text = new Label();
		track_name = new Label();
		track_slider_name = new Label();
		track_current_pos = new Label();
		track_list = FXCollections.observableArrayList();
		file_list = FXCollections.observableHashMap();
		track_view = new ListView<String>(track_list);
		sel_model = track_view.getSelectionModel();
		
		play.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(player != null)
					player.play();
			}
		});
		
		stop.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(player != null)
					player.pause();
			}
		});
		
		get_track.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event)
			{		 
				track = file_chooser.showOpenDialog(my_stage);
				if(track != null) 
				{	
					init_player();
				}
			}
		});
		
		volume.valueProperty().addListener(new ChangeListener<Number>() 
		{
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) 
			{
				if(player != null)
					player.setVolume(volume.getValue() / 100.0);
				volume_text.setText(new Integer((int)volume.getValue()).toString());
			}
		});
		
		track_listener = new ChangeListener<Number> () 
		{
			public void changed(ObservableValue<? extends Number> ov, Number old_value, Number new_Value) 
			{
				if(player != null) 
				{
					player.seek(Duration.millis(track_slider.getValue()));
				}
			}
		};
		
		add_track.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				List<File> list = file_chooser.showOpenMultipleDialog(my_stage);
				if(list != null) 
				{
					for(File new_track : list) 
					{
						track_list.add(new_track.getName());
						file_list.put(new_track.getName(), new_track);
					}
				}
			}
		});
		
		play_tracks.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(!track_list.isEmpty()) 
				{
					select_if_not_selected();
					player.play();
				}
			}
		});
		
		delete_track.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(sel_model.getSelectedItem() != null) 
				{
					file_list.remove(sel_model.getSelectedItem());
					track_list.remove(sel_model.getSelectedItem());
					
					if(track_list.isEmpty() && player != null)
					{
						player.dispose();
					}
				}
			}
		});
		
		save.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event)
			{
				try 
				{
					save_tracks();
				}
				catch(FileNotFoundException exc) 
				{
					show_error("File error", "File not found", AlertType.ERROR);
				}
				catch(IOException exc) 
				{
					show_error("File output error", "File output error", AlertType.ERROR);
				}
			}
		}); 
		
		load.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				try
				{
					load_tracks();
				}
				catch(FileNotFoundException exc) 
				{
					show_error("File error", "File not found", AlertType.ERROR);
				}
				catch(IOException exc) 
				{
					show_error("File input error", "File input error", AlertType.ERROR);
				}
				catch(ClassNotFoundException exc) 
				{
					show_error("File error", "Class not found", AlertType.ERROR);
				}
			}
		});
		
		next_track.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(sel_model.getSelectedIndex() == track_list.size() - 1) 
				{
					select_track(0);
				}
				else 
				{
					select_track(sel_model.getSelectedIndex() + 1);
				}
			}
		});
		
		prev_track.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				if(sel_model.getSelectedIndex() == 0) 
				{
					select_track(track_list.size() - 1);
				}
				else 
				{
					select_track(sel_model.getSelectedIndex() - 1);
				}
			}
		});
			
		alphabetic_sort.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				Collections.sort(track_list, new Comparator<String>() 
				{
					public int compare(String a, String b) 
					{
						return a.compareToIgnoreCase(b);
					}
				});
			}
		}); 
		
		reverse_alpha_sort.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				Collections.sort(track_list, new Comparator<String>() 
				{
					public int compare(String a, String b) 
					{
						return -a.compareToIgnoreCase(b);
					}
				});
			}
		});
		
		search_tracks.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				System.out.println("Done");
				select_found();
			}
		});
		
		search_button.setOnAction(new EventHandler<ActionEvent>() 
		{
			public void handle(ActionEvent event) 
			{
				select_found();
			}
		});
			
		volume.setMin(0);
		volume.setMax(100);
		volume.setValue(50);	
		volume_text.setText(new Integer((int)volume.getValue()).toString());
		
		track_view.setPrefSize(100, 70);
		sel_model.setSelectionMode(SelectionMode.SINGLE);
		file_chooser.setInitialDirectory(new File(Paths.get(".").toAbsolutePath().normalize().toString()));
		
		sort_menu.getItems().addAll(alphabetic_sort, reverse_alpha_sort);
		sort_tracks.setContextMenu(sort_menu);
		search_tracks.setId("Search tracks");
			
		button_layout.setAlignment(Pos.CENTER);
		button_layout.getChildren().addAll(play, stop, get_track);
		volume_layout.setAlignment(Pos.CENTER);
		volume_layout.getChildren().addAll(volume, volume_text);
		track_name_layout.setAlignment(Pos.CENTER);
		track_name_layout.getChildren().addAll(track_name, track_slider_name);
		track_name.setPadding(new Insets(0, 50, 0, 0));
		track_slider_layout.setAlignment(Pos.CENTER);
		track_slider_layout.getChildren().addAll(track_slider, track_current_pos);
		tracks_button_layout.setAlignment(Pos.CENTER);
		tracks_button_layout.getChildren().addAll(add_track, play_tracks);
		save_load_layout.setAlignment(Pos.CENTER);
		save_load_layout.getChildren().addAll(save, load);
		next_buttons_layout.setAlignment(Pos.CENTER);
		next_buttons_layout.getChildren().addAll(prev_track, next_track);
		shuffle_buttons.setAlignment(Pos.CENTER);
		shuffle_buttons.getChildren().addAll(shuffle_tracks, sort_tracks);
				
		root_node.setAlignment(Pos.CENTER);
		root_node.getChildren().addAll(button_layout, volume_layout,
				track_name_layout, track_slider_layout, track_view,
				tracks_button_layout, delete_track, save_load_layout,
				next_buttons_layout, shuffle_buttons, search_tracks,
				search_button);
			
		my_stage.setTitle("Audio Player");
		my_stage.setScene(my_scene);
		my_stage.show();
	}
	
	private String get_duration() 
	{
		Double seconds = new Double(Math.floor(hit.getDuration().toSeconds()));
		Double minutes = new Double(Math.floor(seconds / 60));
		seconds %= 60;

		return to_time(minutes.intValue(), seconds.intValue());
	}
	
	private String get_current_pos_text() 
	{
		Double seconds = new Double(Math.floor(player.getCurrentTime().toSeconds()));
		Double minutes = new Double(Math.floor(seconds / 60));
		seconds %= 60;
		
		return to_time(minutes.intValue(), seconds.intValue());
	}
	
	private String to_time(Integer minutes, Integer seconds) 
	{
		String time = new String();
		
		if(minutes < 10) 
		{
			time += 0;
			time += minutes;
		}
		else 
		{
			time += minutes;
		}
		
		time += " : ";
		
		if(seconds < 10) 
		{
			time += 0;
			time += seconds;
		}
		else 
		{
			time += seconds;
		}
		
		return time;
	}
	
	private void init_player() 
	{
		if(player != null)
		{
			player.dispose();
		}

		try 
		{
			hit = new Media(track.toURI().toString());
		}
		catch(MediaException exc) 
		{
			show_error("Unsupported file format", "This file has an unsupported format", AlertType.ERROR);
			hit = null;
			return;
		}

		player = new MediaPlayer(hit);	
		track_name.setText(track.getName());
	
		player.setOnReady(new Runnable() 
		{
			public void run() 
			{
				if(player != null) 
				{
					track_slider_name.setText(get_duration());
					track_current_pos.setText(get_current_pos_text());
					track_slider.setMin(0);
					track_slider.setMax(hit.getDuration().toMillis());
					track_slider.setValue(0);
					player.setVolume(volume.getValue() / 100.0);
				}
			}
		});
		
		player.currentTimeProperty().addListener(new ChangeListener<Duration>() 
		{
			public void changed(ObservableValue<? extends Duration> ov, Duration old_val, Duration new_val) 
			{
				track_slider.valueProperty().removeListener(track_listener);
				track_current_pos.setText(get_current_pos_text());
				track_slider.setValue(player.getCurrentTime().toMillis());
				track_slider.valueProperty().addListener(track_listener);
			}
		});
		
		player.setOnEndOfMedia(new Runnable() 
		{
			public void run() 
			{
				if(!track_list.isEmpty()) 
				{
					if(sel_model.getSelectedIndex() == track_list.size() - 1) 
					{
						sel_model.select(0);
					}
					else 
					{
					sel_model.selectNext();
					}
					track = file_list.get(sel_model.getSelectedItem());
					init_player();
					player.play();
				}
				track_current_pos.setText(get_current_pos_text());
			}
		});
	}
	
	private void save_tracks() throws FileNotFoundException, IOException
	{
		Stage file_stage = new Stage();
		File file = file_chooser.showSaveDialog(file_stage);
		if(file != null) 
		{
			PrintWriter printer = new PrintWriter(new FileOutputStream(file.getPath()));
			for(String s : track_list) 
			{
				printer.println(file_list.get(s));
			}
			printer.close();
		}
	}
	
	private void load_tracks() throws FileNotFoundException, IOException, ClassNotFoundException
	{
		Stage file_stage = new Stage();
		File file = file_chooser.showOpenDialog(file_stage);
		if(file != null) 
		{
			Scanner scanner = new Scanner(new FileInputStream(file.getPath()));
			while(scanner.hasNext()) 
			{
				File load_file = new File(scanner.next());
				file_list.put(load_file.getName(), load_file);
				track_list.add(load_file.getName());
			}
			scanner.close();
		}
	}
	
	private void select_if_not_selected() 
	{
		if(!track_list.isEmpty()) 
		{
			track = file_list.get(sel_model.getSelectedItem());
			if(track == null) 
			{
				sel_model.select(0);
				track = file_list.get(sel_model.getSelectedItem());
			}
			init_player();
		}
	}
	
	private void show_error(String title, String content, AlertType error_type) 
	{
		Alert error = new Alert(error_type);
		error.setTitle(title);
		error.setContentText(content);
		error.showAndWait();
	}
	
	private void select_track(int index) 
	{
		if(!track_list.isEmpty()) 
		{
			if(sel_model.getSelectedItem() == null) 
			{
				sel_model.select(0);
			}
			sel_model.select(index);
			track = file_list.get(sel_model.getSelectedItem());
			init_player();
		}
	}
	
	private void select_found() 
	{
		int index;
		for(index = 0; index < track_list.size(); ++index) 
		{
			if(track_list.get(index).startsWith(search_tracks.getText())) 
			{
				select_track(index);
				break;
			}
			else if(track_list.get(index).contains(search_tracks.getText())) 
			{
				select_track(index);
				break;
			}
		}
		select_track(index);
	}
	
	public static void main(String[] args) 
	{
		launch();
	}
}
