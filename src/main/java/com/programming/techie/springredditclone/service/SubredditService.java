package com.programming.techie.springredditclone.service;

import com.programming.techie.springredditclone.dto.PostResponse;
import com.programming.techie.springredditclone.dto.SubredditDto;
import com.programming.techie.springredditclone.exception.SubredditNotFoundException;
import com.programming.techie.springredditclone.model.Subreddit;
import com.programming.techie.springredditclone.model.User;
import com.programming.techie.springredditclone.repository.PostRepository;
import com.programming.techie.springredditclone.repository.SubredditRepository;
import com.programming.techie.springredditclone.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.programming.techie.springredditclone.util.Constants.SUBREDDIT_NOT_FOUND_WITH_ID;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;

@Service
@AllArgsConstructor
public class SubredditService {

    private final SubredditRepository subredditRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SubredditDto> getAll() {
        return subredditRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(toList());
    }

    private SubredditDto mapToDto(Subreddit subreddit) {
        return SubredditDto.builder().name(subreddit.getName())
                .postCount(subreddit.getPosts().size())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(Long id) {
        Subreddit subreddit = subredditRepository.findById(id)
                .orElseThrow(() -> new SubredditNotFoundException(SUBREDDIT_NOT_FOUND_WITH_ID + id));
        return postRepository.findAllBySubreddit(subreddit)
                .stream()
                .map(postService::mapToDto)
                .collect(toList());
    }

    @Transactional
    public SubredditDto save(SubredditDto subredditDto) {
        Subreddit subreddit = subredditRepository.save(mapToSubreddit(subredditDto));
        subredditDto.setId(subreddit.getId());
        return subredditDto;
    }

    private Subreddit mapToSubreddit(SubredditDto subredditDto) {
        return Subreddit.builder().name(subredditDto.getName())
                .description(subredditDto.getDescription())
                .user(authService.getCurrentUser())
                .createdDate(now()).build();
    }

    @Transactional
    public void joinSubreddit(SubredditDto subredditDto) {
        User currentUser = authService.getCurrentUser();
        Subreddit subreddit = subredditRepository.findById(subredditDto.getId())
                .orElseThrow(() -> new SubredditNotFoundException(SUBREDDIT_NOT_FOUND_WITH_ID + subredditDto.getId()));
        currentUser.getSubreddits().add(subreddit);
        userRepository.save(currentUser);
    }
}
