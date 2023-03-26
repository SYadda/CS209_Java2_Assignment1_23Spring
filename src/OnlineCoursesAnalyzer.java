import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * please run it on JDK17.
 */
public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  /**
   * load data from file.
   *
   * @param datasetPath The path of csv data file
   */
  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]),
            Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]),
            Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]),
            Double.parseDouble(info[20]),
            Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * required function No.1.
   *
   * @return a map, where the key is the institution while the value is the total number of
   *     participants who have accessed the courses of the institution.
   *     The map should be sorted by the alphabetical order of the institution.
   */
  public Map<String, Integer> getPtcpCountByInst() {
    Map<String, Integer> map = new TreeMap<>();
    for (String s : courses.stream().map(i -> i.institution).distinct().toList()) {
      map.put(s, courses.stream().filter(i -> i.institution.equals(s)).map(i -> i.participants)
          .reduce(Integer::sum).orElse(0));
    }
    return map;
  }

  /**
   * required function No.2.
   *
   * @return a map, where the key is the string concatenating the Institution and the course Subject
   *     (without quotation marks) using '-' while the value is the total number of participants in
   *     a course Subject of an institution.   The map should be sorted by descending order of
   *     count (i.e., from most to least participants). If two participants have the same count,
   *     then they should be sorted by the alphabetical order of the institution-course Subject.
   */
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Map<String, Integer> map = new HashMap<>();
    for (String s : courses.stream().map(i -> i.institution + "-" + i.subject).distinct()
        .toList()) {
      map.put(s, courses.stream().filter(i -> (i.institution + "-" + i.subject).equals(s))
          .map(i -> i.participants).reduce(Integer::sum).orElse(0));
    }
    Map<String, Integer> map2 = new LinkedHashMap<>();
    map.entrySet().stream()
        .sorted((p1, p2) -> Objects.equals(p1.getValue(), p2.getValue()) ? p1.getKey()
            .compareTo(p2.getKey()) : p2.getValue().compareTo(p1.getValue()))
        .forEachOrdered(i -> map2.put(i.getKey(), i.getValue()));
    return map2;
  }

  /**
   * required function No.3.
   *
   * @return a map, where the key is the name of the instructor (without quotation marks) while the
   *     value is a list containing 2-course lists, where List 0 is the instructor's independently
   *     responsible courses, if s/he has no independently responsible courses, this list also
   *     needs to be created, but with no elements. List 1 is the instructor's co-developed
   *     courses, if there are no co-developed courses, do the same as List 0. Note that the course
   *     title (without quotation marks) should be sorted by alphabetical order in the list, and
   *     the case of identical names should be treated as the same person.
   */
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> map = new HashMap<>();
    for (String s : Stream.concat(
        courses.stream().map(i -> i.instructors).filter(i -> !i.contains(",")),
        courses.stream().map(i -> i.instructors).filter(i -> i.contains(",")).distinct()
            .map(i -> i.split(", ")).flatMap(Arrays::stream)).distinct().toList()) {
      map.put(s, Arrays.asList(
          courses.stream().filter(i -> i.instructors.equals(s))
              .map(i -> i.title).distinct().sorted().toList(),
          courses.stream().filter(i -> i.instructors.contains(",")
                  && Arrays.asList(i.instructors.split(", ")).contains(s))
              .map(i -> i.title).distinct().sorted().toList()));
    }
    return map;
  }

  /**
   * required function No.4.
   *
   * @param topK the size of return course list.
   * @param by the method of sort.
   * @return a list of top K Course titles by the given method.
   */
  public List<String> getCourses(int topK, String by) {
    if (by.equals("hours")) {
      return courses.stream().sorted(
          (p1, p2) -> p1.totalHours == p2.totalHours ? p1.title.compareTo(p2.title)
              : (p1.totalHours < p2.totalHours ? 1 : -1))
          .map(i -> i.title).distinct().limit(topK).toList();
    } else if (by.equals("participants")) {
      return courses.stream().sorted(
          (p1, p2) -> p1.participants == p2.participants ? p1.title.compareTo(p2.title)
              : (p1.participants < p2.participants ? 1 : -1))
          .map(i -> i.title).distinct().limit(topK).toList();
    } else {
      return null;
    }
  }

  /**
   * required function No.5.
   *
   * @param courseSubject the subject of each course. Fuzzy matching is supported and
   *     case-insensitive. If the input courseSubject is "science", all courses whose course
   *     subject includes "science" or "Science" or whatever (case-insensitive) meet the criteria.
   * @param percentAudited the percent of the audited should >= percentAudited.
   * @param totalCourseHours the Total Course Hours (Thousands) should <= totalCourseHours.
   * @return a list of course titles that meet the given criteria, and sorted by alphabetical
   *     order of the titles. The same course title can only occur once in the list.
   */
  public List<String> searchCourses(String courseSubject, double percentAudited,
      double totalCourseHours) {
    return courses.stream().filter(
            i -> i.subject.toLowerCase().contains(courseSubject.toLowerCase())
                && i.percentAudited >= percentAudited && i.totalHours <= totalCourseHours)
        .map(i -> i.title).distinct().sorted().toList();
  }

  /**
   * required function No.6.
   *
   * @param age age of the participants
   * @param gender 0-female, 1-male
   * @param isBachelorOrHigher 0-Not get bachelor degree, 1- Bachelor degree or higher
   * @return a list of recommends 10 course titles based on the following input parameter, sorted
   *     by their similarity values.
   */
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Map<String, Double> map = new HashMap<>();
    for (String s : courses.stream().map(i -> i.number).distinct().toList()) {
      long size = courses.stream().filter(i -> i.number.equals(s)).count();
      String thisTitle = courses.stream().filter(i -> i.number.equals(s))
          .sorted((p1, p2) -> p2.launchDate.compareTo(p1.launchDate)).limit(1)
          .map(i -> i.title).toList().get(0);
      Double thisSimilarity = Math.pow((age - (courses.stream().filter(i -> i.number.equals(s))
          .map(i -> i.medianAge).reduce(Double::sum).orElse(0D) / size)), 2)
          + Math.pow((gender * 100 - (courses.stream().filter(i -> i.number.equals(s))
          .map(i -> i.percentMale).reduce(Double::sum).orElse(0D) / size)), 2)
          + Math.pow((isBachelorOrHigher * 100 - (courses.stream().filter(i -> i.number.equals(s))
          .map(i -> i.percentDegree).reduce(Double::sum).orElse(0D) / size)), 2);

      if (!map.containsKey(thisTitle) || map.get(thisTitle) > thisSimilarity) {
        map.put(thisTitle, thisSimilarity);
      }
    }
    return map.entrySet().stream().sorted((p1, p2) -> Objects.equals(p1.getValue(), p2.getValue())
            ? p1.getKey().compareTo(p2.getKey()) : (p1.getValue() < p2.getValue() ? -1 : 1))
        .distinct().limit(10).map(Map.Entry::getKey).toList();
  }
}

