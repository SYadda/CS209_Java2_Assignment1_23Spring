import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

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
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
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

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> map = new TreeMap<>();
        for (String s: courses.stream().map(i -> i.institution).distinct().toList()) {
            map.put(s, courses.stream().filter(i -> i.institution.equals(s)).map(i -> i.participants).reduce(Integer::sum).orElse(0));
        }
        return map;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> map = new HashMap<>();
        for (String s: courses.stream().map(i -> i.institution + "-" + i.subject).distinct().toList()) {
            map.put(s, courses.stream().filter(i -> (i.institution + "-" + i.subject).equals(s)).map(i -> i.participants).reduce(Integer::sum).orElse(0));
        }
        Map<String, Integer> map2 = new LinkedHashMap<>();
        map.entrySet().stream()
            .sorted((p1, p2) -> Objects.equals(p1.getValue(), p2.getValue()) ? p1.getKey().compareTo(p2.getKey()): p2.getValue().compareTo(p1.getValue()))
            .forEachOrdered(i -> map2.put(i.getKey(), i.getValue()));
        return map2;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> map = new HashMap<>();
        for (String s: Stream.concat(courses.stream().map(i -> i.instructors).filter(i -> ! i.contains(",")), courses.stream().map(i -> i.instructors).filter(i -> i.contains(",")).distinct().map(i -> i.split(", ")).flatMap(Arrays::stream)).distinct().toList()) {
            map.put(s, Arrays.asList(courses.stream().filter(i -> i.instructors.equals(s)).map(i -> i.title).distinct().sorted().toList(), courses.stream().filter(i -> i.instructors.contains(",") && Arrays.asList(i.instructors.split(", ")).contains(s)).map(i -> i.title).distinct().sorted().toList()));
        }
        return map;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by.equals("hours")) {
            return courses.stream().sorted((p1, p2) -> p1.totalHours == p2.totalHours ? p1.title.compareTo(p2.title) : (p1.totalHours < p2.totalHours ? 1 : -1)).map(i -> i.title).distinct().limit(topK).toList();
        } else if (by.equals("participants")) {
            return courses.stream().sorted((p1, p2) -> p1.participants == p2.participants ? p1.title.compareTo(p2.title) : (p1.participants < p2.participants ? 1 : -1)).map(i -> i.title).distinct().limit(topK).toList();
        } else {
            return null;
        }
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        return courses.stream().filter(i -> i.subject.toLowerCase().contains(courseSubject.toLowerCase()) && i.percentAudited >= percentAudited && i.totalHours <= totalCourseHours).map(i -> i.title).distinct().sorted().toList();
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, Double> map = new HashMap<>();
        for (String s: courses.stream().map(i -> i.number).distinct().toList()) {
            long size = courses.stream().filter(i -> i.number.equals(s)).count();
            String this_title = courses.stream().filter(i -> i.number.equals(s)).sorted((p1, p2) -> p2.launchDate.compareTo(p1.launchDate)).limit(1).map(i -> i.title).toList().get(0);
            Double this_similarity =
                Math.pow((age -                      (courses.stream().filter(i -> i.number.equals(s)).map(i -> i.medianAge).reduce(Double::sum).orElse(0D) / size)), 2) +
                Math.pow((gender * 100 -             (courses.stream().filter(i -> i.number.equals(s)).map(i -> i.percentMale).reduce(Double::sum).orElse(0D) / size)), 2) +
                Math.pow((isBachelorOrHigher * 100 - (courses.stream().filter(i -> i.number.equals(s)).map(i -> i.percentDegree).reduce(Double::sum).orElse(0D) / size)), 2);

            if (!map.containsKey(this_title) || map.get(this_title) > this_similarity) {
                map.put(this_title, this_similarity);
            }
        }
        return map.entrySet().stream().sorted((p1, p2) -> Objects.equals(p1.getValue(), p2.getValue()) ? p1.getKey().compareTo(p2.getKey()) : (p1.getValue() < p2.getValue() ? -1 : 1)).distinct().limit(10).map(Map.Entry::getKey).toList();
    }
}

class Course {
    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}