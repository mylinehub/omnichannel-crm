import { keyframes, style } from '@angular/animations';

export const wobble = [
    style({transform: 'translate3d(-25%, 0, 0) rotate3d(0, 0, 1, -5deg)', offset: .15}),
    style({transform: 'translate3d(20%, 0, 0) rotate3d(0, 0, 1, 3deg)', offset: .30}),
    style({transform: 'translate3d(-15%, 0, 0) rotate3d(0, 0, 1, -3deg)', offset: .45}),
    style({transform: 'translate3d(10%, 0, 0) rotate3d(0, 0, 1, 2deg)', offset: .60}),
    style({transform: 'translate3d(-5%, 0, 0) rotate3d(0, 0, 1, -1deg)', offset: .75}),
    style({transform: 'none', offset: 1})
]

export const jello = [
    // style({transform: 'none', offset: .111}),
    style({transform: 'skewX(-12.5deg) skewY(-12.5deg)', offset: .111}),
    style({transform: 'skewX(6.25deg) skewY(6.25deg)', offset: .222}),
    style({transform: 'skewX(-3.125deg) skewY(-3.125deg)', offset: .333}),
    style({transform: 'skewX(1.5625deg) skewY(1.5625deg)', offset: .444}),
    style({transform: 'skewX(-0.78125deg) skewY(-0.78125deg)', offset: .555}),
    style({transform: 'skewX(0.390625deg) skewY(0.390625deg)', offset: .666}),
    style({transform: 'skewX(0.390625deg) skewY(0.390625deg)', offset: .777}),
    style({transform: 'skewX(-0.1953125deg) skewY(-0.1953125deg)', offset: .888}),
    style({transform: 'none', offset: 1})
]


export const swing = [
    style({transform: 'rotate3d(0, 0, 1, 15deg)', offset: .2}),
    style({transform: 'rotate3d(0, 0, 1, -10deg)', offset: .4}),
    style({transform: 'rotate3d(0, 0, 1, 5deg)', offset: .6}),
    style({transform: 'rotate3d(0, 0, 1, -5deg)', offset: .8}),
    style({transform: 'none', offset: 1})
]

export const slideOutLeft = [
    style({transform: 'translate3d(0, 0, 0)', offset: 0}),
    style({transform: 'translate3d(-150%, 0, 0)', opacity: 0, offset: 1}),
]

export const zoomOutRight = [
    style({transform: 'scale3d(.475, .475, .475) translate3d(-42px, 0, 0)', offset: .4}),
    style({transform: 'scale(.1) translate3d(2000px, 0, 0)', 'transform-origin': 'right center', offset: 1}),
]

export const rotateOutUpRight = [
    style({ transform: 'rotate3d(0, 0, 0, 0deg)', opacity: 1, 'transform-origin': 'right bottom', offset: 0}),
    style({ transform: 'rotate3d(0, 0, 1, 90deg)', opacity: 0, 'transform-origin': 'right bottom', offset: 1}),
]


export const flip = [
  style({
    transform: 'perspective(400px) scale3d(1, 1, 1) translate3d(0, 0, 0) rotate3d(0, 1, 0, -360deg)',
    offset: 0
  }),
  style({
    transform: 'perspective(400px) scale3d(1, 1, 1) translate3d(0, 0, 150px) rotate3d(0, 1, 0, -190deg)',
    offset: 0.4
  }),
  style({
    transform: 'perspective(400px) scale3d(1, 1, 1) translate3d(0, 0, 150px) rotate3d(0, 1, 0, -170deg)',
    offset: 0.5
  }),
  style({
    transform: 'perspective(400px) scale3d(0.95, 0.95, 0.95) translate3d(0, 0, 0) rotate3d(0, 1, 0, 0deg)',
    offset: 0.8
  }),
  style({
    transform: 'perspective(400px) scale3d(1, 1, 1) translate3d(0, 0, 0) rotate3d(0, 1, 0, 0deg)',
    offset: 1
  })
];

export const flipInX = [
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, 90deg)',
    opacity: 0,
    offset: 0
  }),
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, -20deg)',
    offset: 0.4
  }),
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, 10deg)',
    opacity: 1,
    offset: 0.6
  }),
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, -5deg)',
    offset: 0.8
  }),
  style({
    transform: 'perspective(400px)',
    offset: 1
  })
];


export const flipInY = [
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, 90deg)',
    opacity: 0,
    offset: 0
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, -20deg)',
    offset: 0.4
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, 10deg)',
    opacity: 1,
    offset: 0.6
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, -5deg)',
    offset: 0.8
  }),
  style({
    transform: 'perspective(400px)',
    offset: 1
  })
];


export const flipOutX = [
  style({
    transform: 'perspective(400px)',
    offset: 0
  }),
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, -20deg)',
    opacity: 1,
    offset: 0.3
  }),
  style({
    transform: 'perspective(400px) rotate3d(1, 0, 0, 90deg)',
    opacity: 0,
    offset: 1
  })
];


export const flipOutY = [
  style({
    transform: 'perspective(400px)',
    offset: 0
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, -15deg)',
    opacity: 1,
    offset: 0.3
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, 90deg)',
    opacity: 0,
    offset: 1
  })
];

export const rotateOutY = [
  style({
    transform: 'perspective(400px)',
    offset: 0
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, -15deg)',
    opacity: 1,
    offset: 0.2
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, 100deg)',
    opacity: 0,
    offset: 0.65
  }),
  style({
    transform: 'perspective(400px) rotate3d(0, 1, 0, 180deg)',
    opacity: 1,
    offset: 1
  })
];


export const bounce = [
  style({
    transform: 'translate3d(0, 0, 0)',
    offset: 0
  }),
  style({
    transform: 'translate3d(0, 0, 0)',
    offset: 0.2
  }),
  style({
    transform: 'translate3d(0, -30px, 0) scaleY(1.1)',
    offset: 0.4
  }),
  style({
    transform: 'translate3d(0, -30px, 0) scaleY(1.1)',
    offset: 0.43
  }),
  style({
    transform: 'translate3d(0, 0, 0)',
    offset: 0.53
  }),
  style({
    transform: 'translate3d(0, -15px, 0) scaleY(1.05)',
    offset: 0.7
  }),
  style({
    transform: 'translate3d(0, 0, 0) scaleY(0.95)',
    offset: 0.8
  }),
  style({
    transform: 'translate3d(0, -4px, 0) scaleY(1.02)',
    offset: 0.9
  }),
  style({
    transform: 'translate3d(0, 0, 0)',
    offset: 1
  })
];
