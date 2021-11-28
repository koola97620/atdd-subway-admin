package nextstep.subway.line.domain;

import nextstep.subway.line.exception.AlreadyRegisteredSectionException;
import nextstep.subway.line.exception.LongDistanceException;
import nextstep.subway.line.exception.NotFoundUpAndDownStation;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Embeddable
public class Sections {

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Section> sections = new ArrayList<>();

    protected Sections() {
    }

    public Sections(Section section) {
        sections.add(section);
    }

    public List<StationResponse> getStations() {
        Section firstSection = findFirstSection();
        List<StationResponse> stationResponses = new ArrayList<>();
        stationResponses.add(StationResponse.of(firstSection.getUpStation()));
        stationResponses.add(StationResponse.of(firstSection.getDownStation()));

        Optional<Section> nextSectionOptional = findNextSection(firstSection);
        while (nextSectionOptional.isPresent()) {
            Section nextSection = nextSectionOptional.get();
            stationResponses.add(StationResponse.of(nextSection.getDownStation()));
            nextSectionOptional = findNextSection(nextSection);
        }
        return stationResponses;
    }

    private Section findFirstSection() {
        List<Station> upStations = new ArrayList<>();
        List<Station> downStations = new ArrayList<>();
        for (Section section : sections) {
            upStations.add(section.getUpStation());
            downStations.add(section.getDownStation());
        }
        upStations.removeAll(downStations);
        Station firstStation = upStations.get(0);

        return sections.stream()
                .filter(section -> section.getUpStation().equals(firstStation))
                .findAny()
                .get();
    }

    private Optional<Section> findNextSection(Section section) {
        return this.sections
                .stream()
                .filter(it -> it.getUpStation().equals(section.getDownStation()))
                .findFirst();
    }

    public void addSection(Section inputSection) {
        checkExistsUpAndDownStations(inputSection);
        checkExistsSameSection(inputSection);

        createInnerSection(inputSection);
        if (!isRegisteredSection(inputSection)) {
            createOuterSection(inputSection);
        }
    }

    private void createInnerSection(Section inputSection) {
        this.sections.stream()
                .filter(section -> isCreateInnerSection(section, inputSection))
                .findAny()
                .ifPresent(savedSection -> {
                    checkSectionDistance(inputSection, savedSection);
                    List<Section> sections = savedSection.createInnerSection(inputSection);
                    int index = this.sections.indexOf(savedSection);
                    this.sections.remove(savedSection);
                    this.sections.addAll(index, sections);
                });

    }

    private void checkExistsUpAndDownStations(Section inputSection) {
        if (isNotExistsUpAndDownStation(inputSection)) {
            throw new NotFoundUpAndDownStation();
        }
    }

    private void checkExistsSameSection(Section inputSection) {
        if (isRegisteredSection(inputSection)) {
            throw new AlreadyRegisteredSectionException();
        }
    }

    private void checkSectionDistance(Section inputSection, Section savedSection) {
        if (inputSection.isLongDistance(savedSection)) {
            throw new LongDistanceException();
        }
    }

    private void createOuterSection(Section inputSection) {
        this.sections.stream()
                .filter(section -> isCreateOuterSection(section, inputSection))
                .findAny()
                .ifPresent(savedSection -> {
                    Section newSection = savedSection.createOuterSection(inputSection);
                    int index = this.sections.indexOf(savedSection);
                    if (savedSection.isCreateUpSection(inputSection)) {
                        this.sections.add(index, newSection);
                        return;
                    }
                    this.sections.add(index + 1, newSection);
                });
    }

    private boolean isCreateInnerSection(Section savedSection, Section inputSection) {
        return savedSection.equalsUpStation(inputSection) || savedSection.equalsDownStation(inputSection);
    }

    private boolean isCreateOuterSection(Section savedSection, Section inputSection) {
        return savedSection.isCreateUpSection(inputSection) || savedSection.isCreateDownSection(inputSection);
    }

    private boolean isNotExistsUpAndDownStation(Section section) {
        return sections.stream()
                .allMatch(savedSection -> savedSection.isNotContainUnAndDownStation(section));
    }

    private boolean isRegisteredSection(Section inputSection) {
        return sections.stream()
                .anyMatch(savedSection -> savedSection.equalsUpAndDownStations(inputSection));
    }

}
